/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * Kizzy.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.my.vortex.feature_rpc_base.services.CustomRpcService
import com.my.vortex.preference.Prefs
import kotlinx.coroutines.delay

private const val PERMISSION_ACKNOWLEDGED = "vortex_new_setup_notifications_acknowledged"

internal fun ComponentActivity.notificationsReady(): Boolean {
    val granted = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
        this, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
    return granted && getSystemService(NotificationManager::class.java)?.areNotificationsEnabled() == true
}

@Composable
internal fun ComponentActivity.Vortex() {
    var launchComplete by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!launchComplete) {
            delay(1100)
            launchComplete = true
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationsEnabled by remember { mutableStateOf(notificationsReady()) }
    var acknowledged by remember { mutableStateOf(Prefs[PERMISSION_ACKNOWLEDGED, false]) }
    var signedIn by remember {
        mutableStateOf(Prefs[Prefs.TOKEN, ""].isNotBlank() && Prefs[Prefs.USER_ID, ""].isNotBlank())
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) notificationsEnabled = notificationsReady()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    when {
        !launchComplete -> LaunchScreen()
        startupStage(notificationsEnabled && acknowledged, signedIn) == StartupStage.PERMISSIONS ->
            PermissionSetup(
                notificationsEnabled = notificationsEnabled,
                onRefresh = { notificationsEnabled = notificationsReady() },
                onContinue = {
                    if (notificationsReady()) {
                        Prefs[PERMISSION_ACKNOWLEDGED] = true
                        acknowledged = true
                    } else notificationsEnabled = false
                }
            )
        !signedIn -> SignInScreen(onCompleted = {
            signedIn = Prefs[Prefs.TOKEN, ""].isNotBlank() && Prefs[Prefs.USER_ID, ""].isNotBlank()
        })
        else -> ConnectedScreen(onSignOut = {
            stopService(Intent(this, CustomRpcService::class.java))
            listOf(Prefs.TOKEN, Prefs.USER_DATA, Prefs.USER_ID, Prefs.USER_BIO, Prefs.USER_NITRO, Prefs.ACTIVE_CUSTOM_RPC_MODE)
                .forEach(Prefs::remove)
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
            signedIn = false
        })
    }
}
