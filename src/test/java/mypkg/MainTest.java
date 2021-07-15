package mypkg;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.mitmproxy.InterceptedMessage;
import io.appium.mitmproxy.MitmproxyJava;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;

public class MainTest {
  private static AppiumDriverLocalService service;
  private static AndroidDriver<WebElement> driver;

  private MitmproxyJava proxy;
  private List<InterceptedMessage> messages;

  @BeforeClass
  public static void setupGlobal() throws Exception {
    service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder().usingAnyFreePort()
        .withArgument(() -> "--allow-insecure", "chromedriver_autodownload")
        .withArgument(() -> "--chromedriver-executable", "./chromedriver_69_to_71"));

    service.start();

    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability("platformName", "Android");
    capabilities.setCapability("deviceName", "emulator-5554");
    capabilities.setCapability("browserName", "Chrome");
    driver = new AndroidDriver<WebElement>(service.getUrl(), capabilities);

    driver.startRecordingScreen();
  }

  @AfterClass
  public static void teardownGlobal() throws Exception {
    String video = driver.stopRecordingScreen();
    byte[] decoded = Base64.getDecoder().decode(video);
    ByteSink sink = Files.asByteSink(new File("appium_recording.mp4"));
    sink.write(decoded);

    if (driver != null) {
      driver.quit();
    }
    if (service != null) {
      service.stop();
    }
  }

  @Before
  public void setup() throws Exception {
    messages = new ArrayList<>();
    proxy = new MitmproxyJava("/usr/local/bin/mitmdump", (InterceptedMessage m) -> {
      messages.add(m);
      return m;
    });
    proxy.start();
  }

  @After
  public void teardown() throws Exception {
    proxy.stop();
    for (InterceptedMessage m : messages) {
      System.out.println("Intercepted Network Requests: ");
      System.out.println("Intercepted request for " + m.requestURL);
      System.out.println("Response headers:");
      for (String[] header : m.responseHeaders) {
        for (String elem : header) {
          System.out.print(elem + " ");
        }
        System.out.println("\n");
      }
    }
  }

  public WebElement getMirrorTestingAd() throws Exception {
    driver.get(new URI("http://www.youtube.com").toString());

    WebElement searchBtn = driver.findElementByCssSelector(".icon-button.topbar-menu-button-avatar-button");
    searchBtn.click();
    Thread.sleep(2000);

    WebElement searchInput = driver.findElementByCssSelector(".searchbox-input.title");
    searchInput.sendKeys("mirrortesting");
    searchInput.sendKeys(Keys.ENTER);
    Thread.sleep(5000);

    // TODO(wzong): wait for element to appear.
    List<WebElement> sparkleAdUrls = driver.findElementsByCssSelector(".promoted-sparkles-text-search-display-url");
    for (WebElement elem : sparkleAdUrls) {
      if (elem.getText().contains("mirrortesting")) {
        return elem;
      }
    }
    return null;
  }

  public WebElement getMirrorTestingAdWithRetry(int numRetries) throws Exception {
    WebElement sparkleAdUrl = null;
    for (int i = 0; i < numRetries; ++i) {
      sparkleAdUrl = getMirrorTestingAd();
      if (sparkleAdUrl != null) {
        break;
      }
    }
    return sparkleAdUrl;
  }

  @Test
  public void testMirrorAd() throws Exception {
    WebElement sparkleAdUrl = getMirrorTestingAdWithRetry(5);
    Assert.assertNotNull(sparkleAdUrl);
  }
}
