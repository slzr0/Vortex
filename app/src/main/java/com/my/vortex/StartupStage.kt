package com.my.vortex

internal enum class StartupStage { PERMISSIONS, SIGN_IN, COMPLETE }

internal fun startupStage(notificationsEnabled: Boolean, acknowledged: Boolean, signedIn: Boolean): StartupStage = when {
    !notificationsEnabled || !acknowledged -> StartupStage.PERMISSIONS
    !signedIn -> StartupStage.SIGN_IN
    else -> StartupStage.COMPLETE
}
