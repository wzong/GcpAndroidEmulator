#!/bin/bash

set -e

export ANDROID_HOME=/var/projectmirror/androidsdk
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
 
# Install JRE/JDK
sudo apt install default-jre default-jdk
 
# Installation for 64bit Linux machine 
sudo dpkg --add-architecture i386
sudo apt update
sudo apt install libc6:i386 libncurses5:i386 libstdc++6:i386
 
# Install Android SDK
mkdir -p $ANDROID_HOME/cmdline-tools/
mkdir -p $ANDROID_HOME/downloads/
cd $ANDROID_HOME/downloads/
wget https://dl.google.com/android/repository/commandlinetools-linux-7302050_latest.zip
unzip commandlinetools-linux-7302050_latest.zip
mv cmdline-tools $ANDROID_HOME/cmdline-tools/latest
 
# Install selendroid
wget https://github.com/selendroid/selendroid/releases/download/0.17.0/selendroid-standalone-0.17.0-with-dependencies.jar
 
# Install Android SDK tools
yes | sdkmanager "platform-tools" "platforms;android-28" "emulator"
yes | sdkmanager "system-images;android-28;google_apis;x86_64"

# Add user to kvm group
sudo gpasswd -a $USER kvm

