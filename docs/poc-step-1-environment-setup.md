# POC Step 1 - Environment Setup

This document maps directly to Step 1 from the plan in docs/prompt_conversation_history.md.

## What is already prepared in this repository

1. Android project scaffold (Compose + Kotlin) created.
2. Min SDK set to 26.
3. Main screen shows Hello World.

### Key files
- Project settings: settings.gradle.kts
- Root build config: build.gradle.kts
- App module config: app/build.gradle.kts
- Main activity: app/src/main/java/com/familyexpensetracker/MainActivity.kt
- Manifest: app/src/main/AndroidManifest.xml

## What you need to do on your machine to complete Step 1

1. Install Android Studio (latest stable).
2. Install JDK 17 (if not already bundled/selected in Android Studio).
3. Open this repository in Android Studio.
4. In SDK Manager, install at least:
   - Android SDK Platform 34
   - Android SDK Build-Tools
   - Android Emulator
5. Create an emulator (AVD), for example Pixel 6 / API 34.
6. Let Android Studio sync Gradle and create wrapper files if prompted.
7. Click Run and verify app launches with Hello World centered on screen.

## Optional device check

1. Enable Developer Options on phone.
2. Enable USB debugging.
3. Connect device and allow debugging prompt.
4. Run app on physical device from Android Studio.

## Step 1 success criteria

- App runs on emulator.
- Hello World is visible.
- You can identify these project areas:
  - app/src/main/
  - AndroidManifest.xml
  - build.gradle.kts
  - MainActivity.kt
