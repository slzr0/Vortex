# Vortex

Vortex is an Android Discord Rich Presence client based on the open-source
[Kizzy project](https://github.com/dead8309/Kizzy). It uses Kotlin, Jetpack
Compose and Material 3.

## Project structure

- `app/` — application entry point, setup flow and navigation.
- `feature_home/`, `feature_apps_rpc/`, `feature_media_rpc/`,
  `feature_custom_rpc/` — presence dashboard and configuration screens.
- `feature_console_rpc/`, `feature_experimental_rpc/`, `feature_profile/`,
  `feature_settings/`, `feature_about/`, `feature_logs/`, `feature_startup/`,
  `feature_crash_handler/` — additional features and settings.
- `feature_rpc_base/` — presence services, notifications, shortcuts and tile.
- `common/` — reusable components, navigation, resources and preferences.
- `theme/`, `color/` — Material theme and palette generation.
- `domain/`, `data/`, `gateway/` — models, repositories, Discord gateway and
  presence implementation.
- `build-logic/`, `gradle/` — convention plugins and dependency versions.

## Build

Open the project in Android Studio with Android SDK 36 and JDK 17,
then build the `app` configuration, or run:

```sh
bash gradlew :app:assembleDebug
```

The project resolves dependencies from Google and Maven repositories. Runtime
permissions, a Discord account and supported Android services are required for
some features. Discord gateway use is at your own risk.

## Compatibility and attribution

The existing Android application ID, preference keys, shortcut actions and
notification channel are retained for compatibility with existing installations.
Source namespaces and primary components have been renamed. Small legacy
component adapters preserve notification access, pinned shortcuts and existing
quick-settings tiles where Android retains those component grants. Upstream documentation, API endpoints and update
links also still refer to the original project; they have not been redirected to
an independent Vortex service. In particular, the update checker queries the
upstream release feed, not a Vortex release feed.

This project is derived from [Kizzy](https://github.com/dead8309/Kizzy); see
`License` and the original source for license and attribution information.

## Discord OAuth login

Sign-in opens the Vortex OAuth server in the browser. Its `vortex://callback`
redirect supplies an access token and Discord profile to the app. The token is
stored in app-private Preferences DataStore (excluded from Android backup), and
logout clears that session. It does not revoke the token at Discord.

The existing RPC implementation is unchanged: it still reads its original RPC
credentials, not this OAuth token. A Discord OAuth access token must not be
assumed to be a Discord Gateway user token. Consequently OAuth sign-in alone
does not establish RPC connectivity.

The current server contract puts a bearer token in a custom-scheme redirect and
does not provide a state parameter. Other apps can register the same scheme, and
callbacks cannot be bound to an initiating login. Before production use, update
the server and client to use a verified HTTPS App Link, an authorization-code
exchange with PKCE, and a validated state value. The current DataStore is private
to the app but is not encrypted at rest.

Instrumented callback and session tests can be run on a connected Android device
with `bash gradlew :app:connectedDebugAndroidTest`.
