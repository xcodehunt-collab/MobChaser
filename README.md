# Metro Denim Launcher

A native Android launcher inspired by the Windows Phone 8.1 Denim Start screen. It can run as a regular app or be selected as the device's default Home app.

## Features

- Native HOME intent support and standalone app entry
- Discovers and launches installed applications
- Metro-style Start screen and alphabetical app drawer
- Search-as-you-type app filtering
- Long-press tiles to open, unpin, or inspect app info
- Long-press drawer entries to pin them to Start
- Persistent pinned apps, light/dark theme, and eight accent colors
- Default launcher role request on Android 10+ and Home settings fallback on Android 8/9
- Wallpaper and Android system settings shortcuts
- No internet permission, ads, analytics, account, or data collection

## Build

Open the folder in Android Studio (JDK 17), allow Gradle sync, then select **Build > Build APK(s)**. The debug APK will be under `app/build/outputs/apk/debug/`.

Command line (with Android SDK installed):

```bash
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

## Install and use

1. Install the APK and open **Metro Denim Launcher** for standalone mode.
2. Open its Settings page and tap **make default Home app**.
3. Choose Metro Denim Launcher and tap the phone's Home button.
4. Long-press a tile to unpin it. Long-press an app in the app list to pin it.

## Compatibility and policy note

The project has `minSdk 26` (Android 8) and `targetSdk 35`. Launcher apps legitimately need visibility of launchable applications. If publishing to Google Play, document the launcher's core purpose when declaring package visibility.
