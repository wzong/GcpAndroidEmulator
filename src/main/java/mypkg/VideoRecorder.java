package mypkg;

import java.io.IOException;

public class VideoRecorder {

  private Process process;

  public void start() throws IOException, InterruptedException {
    this.delete();
    String homeDir = System.getProperty("user.home");
    System.out.println("Starting screen recording");
    process = Runtime.getRuntime()
        .exec(homeDir + "/androidsdk/platform-tools/adb shell 'screenrecord --bit-rate 4000000 /sdcard/video.mp4'");
  }

  public void stop() throws IOException, InterruptedException {
    System.out.println("Stopping screen recording");
    byte[] ctrlBreak = { 0x3 };
    process.getOutputStream().write(ctrlBreak);
    process.waitFor();
  }

  public void save() throws IOException, InterruptedException {
    String homeDir = System.getProperty("user.home");
    System.out.println("Downloading recorded video");
    Process download = Runtime.getRuntime().exec(homeDir + "/androidsdk/platform-tools/adb pull /sdcard/video.mp4");
    if (download.waitFor() != 0) {
      throw new RuntimeException("Failed to download recorded video");
    }
  }

  public void delete() throws IOException, InterruptedException {
    String homeDir = System.getProperty("user.home");
    Process download = Runtime.getRuntime()
        .exec(homeDir + "/androidsdk/platform-tools/adb shell rm -f /sdcard/video.mp4");
    if (download.waitFor() != 0) {
      throw new RuntimeException("Failed to delete recorded video");
    }
  }
}
