# Vortex

A fresh Android project containing the first Vortex setup flow: loading, notification access, and Discord sign-in. The previous app features and modules are not part of this project.

## Project structure

- `app/`: single Kotlin, Jetpack Compose and Material 3 Android application.
- `gradle/`, `gradlew`: Gradle wrapper.
- `.github/workflows/build.yml`: debug APK build and artifact upload.

The minimum supported version is Android 9 (API 28). Android 9–14 are the intended versions; no maximum OS version is enforced. On Android 13 and later, the app requests notification permission; on older versions it checks whether notifications are enabled in system settings. Notifications are requested for future background activity, which is not yet implemented.

The app has a new application ID, `com.my.vortex`, so it installs separately from the previous APK and does not migrate its data.

## Discord sign-in

This first version retains the previous unofficial in-app WebView sign-in. It extracts a user token and checks it against Discord before saving it encrypted with Android Keystore. Discord may change or block this approach, and it may violate Discord's terms. Sign-out removes the local session and WebView data; it does not revoke the Discord session. App backup is disabled. Never share the token.

## Build

Use JDK 17 and Android SDK 36 and run `./gradlew assembleDebug`. The workflow builds on pushes to `main` and can be started manually, uploading the debug APK as an artifact.

The retained WebView source includes its original copyright notice. See [License](License).
