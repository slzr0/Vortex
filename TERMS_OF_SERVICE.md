# Vortex Privacy and Terms

Vortex is an unofficial Android client. Discord and other external services have their own terms and privacy policies, which apply when you use them.

## Data and permissions

The app stores its settings and Discord user token in app-private MMKV preferences. This storage is not encrypted by Vortex. Signing out removes the locally stored token and profile fields but does not revoke the Discord session. App data can also be deleted through Android settings.

Vortex sends data to Discord and may contact third-party services for game presets, profile details, images and update checks. These services can receive network information and the data needed to fulfill requests. Do not use sensitive information in your presence or image uploads.

App notifications and foreground-service permissions keep presence services visible. On supported Android versions, Vortex asks you to enable notifications before signing in. Storage access may be used for configuration files on older Android versions. Games and Custom do not require usage access or notification-listener access.

Use of a Discord user token and unofficial automation may violate Discord's terms and put your account at risk. Service availability is not guaranteed.

## License

Vortex is distributed under the terms in [License](License). Original copyright and attribution notices remain in the source files.
