# GcpSelendroid

## Environment Setup

1. Create VM instance on GCP and enable nested virtualization.

    By default GCP
    Compute Engine VM does not enable nested virtualization. We need to follow
    [this guide](https://cloud.google.com/community/tutorials/setting-up-an-android-development-environment-on-compute-engine)
    to enable nested virtualization. (I used `--image-family ubuntu-2004-lts`
    but default debian should also work).

    The above guide also contains instructions for setting up VNC server, that
    allows accessing the VM with GUI. This allows us to view the screen of the
    emulator remotely (otherwise we can only bring up a headless emulator and
    record screen for troubleshooting).

2. Install Android SDK and tools

    First SSH into the VM (easiest way is via GCP Compute Engine Web UI).
    And execute the following command:

    ```shell
    sudo apt install git
    git clone https://github.com/wzong/GcpAndroidEmulator.git
    cd GcpAndroidEmulator
    bash install.sh
    ```

    The script will also create 2 emulator images with API level 27 and 28.

3. Set environment variable

    Execute the following or add them into `~/.bashrc` or `/etc/profile`

    ```shell
    export ANDROID_HOME=/var/projectmirror/androidsdk
    export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
    export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
    ```

4. Start Android Emulator

    If using SSH to access the VM, emulator can be started headlessly:

    ```shell
    emulator -avd avd27 -no-audio -no-window -writable-system &
    ```

    If using VncViewer to access the VM via GUI, we can start emulator with GUI:

    ```shell
    emulator -avd avd27 -writable-system
    ```

    I have run into error of `/dev/kvm device: permission denied` even though
    `$USER` is already member of `kvm` group. And I fixed with:

    ```shell
    sudo chown $USER -R /dev/kvm
    ```

5. Set up Network Proxy ([guide](https://appiumpro.com/editions/63-capturing-android-emulator-network-traffic-with-appium))

    ```shell
    adb root
    adb remount
    ca=~/.mitmproxy/mitmproxy-ca-cert.pem
    hash=$(openssl x509 -noout -subject_hash_old -in $ca)
    adb push $ca /system/etc/security/cacerts/$hash.0
    adb unroot
    ```

6. Restart Android Emulator with Network Proxy

    ```shell
    emulator -avd avd27 -http-proxy http://0.0.0.0:8080
    ```

    The Appium automation below will start the proxy server at 8080 port
    in the Java application/test. For manual troubleshooting, we can also
    start the proxy server in terminal:

    ```shell
    mitmproxy
    ```

## Android Automation

I've used [Appium](https://appium.io/) to automate Android Chrome. The framework
supports all of Android/iOS Web/Native/Hybrid Apps.

* [Selendroid](http://selendroid.io/): After a few attempt with the latest
  `0.17.0` version, I found it does not support API > 23 (Android 6). And
  also I run into a lot of other issues.
* [Espresso](https://developer.android.com/training/testing/espresso): This is
  widely used, but has limitations e.g. does not support Chrome or other native
  Android Apps (like YouTube)
* [UIAutomator](https://developer.android.com/training/testing/ui-automator):
  Nothing to complain, but Appium has provided a nice wrapper on top of
  UIAutomator, so that I can use the popular Selenium APIs to automate
  Android Apps and Web pages, much less learning curve for me.

Installations below:

  1. Install/Upgrade NodeJS and NPM:

      ```shell
      sudo apt update
      sudo apt install nodejs
      nodejs -v # Expect >= v10.19.0
      sudo apt install npm
      sudo npm install -g npm
      npm -v # Expect >= 7.19.1
      ```

  2. Install Appium (Following [link](https://appium.io/docs/en/about-appium/getting-started)):

      ```shell
      sudo npm install -g appium
      ```

  3. Download ChromeDriver for Android.

      The default ChromeDriver does not support newer versions of Android
      Chrome that comes with API 27/28 emulators. I've downloaded one in
      `chromedriver_69_to_70` that works for Android Chrome `69.0.3497.100`.
      A complete list is [here](https://chromedriver.chromium.org/downloads).

# Android Chrome Automation

Sample code developed at `src/test/java/mypkg/MainTest.java`. Video was
captured at `appium_recording.mp4`. Execute `mvn test` to rerun.
