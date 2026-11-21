# Vortex

Vortex is being rebuilt with Kotlin, Jetpack Compose and Material 3. This first stage contains a new loading screen, notification-access setup and in-app Discord sign-in. After sign-in, a confirmation screen allows signing out. The previous Games, Custom and Settings screens are not exposed in this build; their source remains in the repository for possible later work.

## Android support

The app's minimum version is Android 9 (API 28). This rebuild is intended for Android 9 through Android 14 (API 34). Android 13 and later use the runtime notification permission; on older versions, notifications can be enabled in system settings if disabled. No maximum SDK restriction is imposed, but versions beyond Android 14 have not been verified.

## Sign-in and privacy

The existing unofficial Discord WebView sign-in extracts a user token and stores it in app-private MMKV preferences without encryption. It may violate Discord's terms or stop working. Sign-out clears local session data, but does not revoke the Discord session. MMKV data is excluded from cloud backup and device transfer. See [Privacy and Terms](TERMS_OF_SERVICE.md).

## Build

Use JDK 17 and Android SDK 36, then run `./gradlew assembleDebug`. GitHub Actions builds and uploads the debug APK for pushes to `main` or manual runs.

## Attribution

Original copyright and attribution notices remain in the source and in [License](License). Bundled game catalog data and its attribution remain in [catalog attribution](data/src/main/assets/catalog/ATTRIBUTION.txt), including the [ODbL](data/src/main/assets/licenses/nintendo-odbl.txt) and [MIT](data/src/main/assets/licenses/xbox-mit.txt) notices.
