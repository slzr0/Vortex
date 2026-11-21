package com.my.vortex

internal enum class StartupStage { PERMISSIONS, SIGN_IN, MAIN }

internal fun startupStage(notificationsReady: Boolean, signedIn: Boolean): StartupStage = when {
    !notificationsReady -> StartupStage.PERMISSIONS
    !signedIn -> StartupStage.SIGN_IN
    else -> StartupStage.MAIN
}
