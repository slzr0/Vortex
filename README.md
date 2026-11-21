# Vortex

Vortex is an Android Discord Rich Presence client built with Kotlin, Jetpack Compose and Material 3.
Choose a game or create a custom activity from a streamlined two-mode workspace.

## Presence modes

- **Console:** search the game catalog, select a preset, or create a manual game and platform preset when the catalog is unavailable. Your last started preset is saved for quick access.
- **Custom:** configure your own presence, images, timestamps and buttons.

Only one presence service should run at a time. A running service indicates the Android service is active; it does not guarantee a Discord connection.

## Getting started

First enable app notifications when requested, then sign in to Discord. If notifications are already enabled, setup proceeds directly to sign-in. Games and Custom do not require usage access or notification-listener access. Console includes a bundled game catalog, supplements it with the remote catalog when available, and also supports manual presets. Artwork and Discord presence still require a network connection.

## Troubleshooting

- Sign in after the permission step before starting an activity. If the permission prompt is denied, use notification settings and return to Vortex.
- If a title is missing, try another spelling or create a manual preset. Bundled catalogs may not list every game or region.
- The gateway retries transient disconnections and restores the latest activity. Keep the foreground notification enabled and allow unrestricted battery usage for better reliability; the wake lock can increase battery use. Android, network outages, force-stop, reboots and Discord restrictions can still interrupt presence, so continuous 24/7 operation is not guaranteed.
- If artwork is missing, verify internet access and image services; text presence can still work without artwork.
- If presence does not appear, check the app logs and your Discord connection. Discord may change or restrict this unofficial integration.

## Build

Use Android Studio, Android SDK 36 and JDK 17, or run:

```sh
bash gradlew :app:assembleDebug
```

A GitHub Actions workflow builds the debug APK on pushes to `main` and can also be run manually.

## Privacy and compatibility

The sign-in screen uses an in-app WebView and retrieves a Discord user token, which the existing RPC implementation stores in app-private MMKV preferences without encryption. MMKV preferences are excluded from Android cloud backup and device transfer to avoid copying the token; settings and favorites stored there will not transfer either. Signing out clears the locally saved token and profile fields; it does not revoke the Discord session. This unofficial method may stop working and may violate Discord's terms. See [Privacy and Terms](TERMS_OF_SERVICE.md).

Existing application identifiers and preference keys remain for upgrade compatibility. App detection, media and experimental entry points are no longer exposed. Third-party services are still used for supplemental catalog entries, artwork, profile information and image handling. Vortex release checks target this repository; no release may be available yet.

This is a derivative open-source project. Original copyright and attribution notices are retained in source files; see [License](License).

## Credits

- [Read You](https://github.com/Ashinch/ReadYou) and [Seal](https://github.com/JunkFood02/Seal) — UI components.
- [Material Color Utilities](https://github.com/material-foundation/material-color-utilities) — color utilities.
- [Rich-Presence-U](https://github.com/ninstar/Rich-Presence-U) and [Rich Presence U Database](https://github.com/ninstar/Rich-Presence-U-DB) — Nintendo and Wii U game data, under the [Open Database License 1.0](data/src/main/assets/licenses/nintendo-odbl.txt). Original CSV files and source revision are included in [catalog attribution](data/src/main/assets/catalog/ATTRIBUTION.txt).
- [Logra](https://github.com/wingio/Logra) — logs UI.
- [Xbox-Rich-Presence-Discord](https://github.com/MrCoolAndroid/Xbox-Rich-Presence-Discord) — Xbox game data, under the [MIT License](data/src/main/assets/licenses/xbox-mit.txt); original JSON and source revision are included in the catalog assets.
- [Monet](https://github.com/Kyant0/Monet) — Material 3 palettes.

## Console tools

Save catalog or manual presets as favorites, filter games by platform, and choose your Discord status and whether elapsed time is shown. Console options take effect when you start or update the presence. The background-readiness card links to Android battery and notification settings; manufacturers can impose additional restrictions, and those settings cannot guarantee continuous presence.
