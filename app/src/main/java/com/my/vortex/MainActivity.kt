package com.my.vortex

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { VortexApp() }
    }

    private fun notificationsEnabled(): Boolean {
        val granted = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return granted && getSystemService(NotificationManager::class.java).areNotificationsEnabled()
    }

    private fun notificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        if (runCatching { startActivity(intent) }.isFailure && runCatching { startActivity(fallback) }.isFailure) {
            Toast.makeText(this, "Unable to open notification settings.", Toast.LENGTH_LONG).show()
        }
    }

    @Composable
    private fun VortexApp() {
        val store = remember { SessionStore(this) }
        var launched by rememberSaveable { mutableStateOf(false) }
        var enabled by remember { mutableStateOf(notificationsEnabled()) }
        var acknowledged by remember { mutableStateOf(store.acknowledged) }
        var signedIn by remember { mutableStateOf(store.hasSession()) }
        var asked by rememberSaveable { mutableStateOf(false) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            asked = true
            enabled = notificationsEnabled()
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) enabled = notificationsEnabled()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        LaunchedEffect(Unit) {
            if (!launched) { delay(1000); launched = true }
        }
        if (!launched) {
            LoadingScreen()
        } else when (startupStage(enabled, acknowledged, signedIn)) {
            StartupStage.PERMISSIONS -> PermissionScreen(enabled, asked,
                onAllow = {
                    if (Build.VERSION.SDK_INT >= 33 && !asked) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else notificationSettings()
                },
                onSettings = ::notificationSettings,
                onContinue = {
                    enabled = notificationsEnabled()
                    if (enabled) { store.acknowledged = true; acknowledged = true }
                })
            StartupStage.SIGN_IN -> SignInScreen(onToken = { token ->
                try { store.save(token); signedIn = true; true } catch (_: Exception) { false }
            })
            StartupStage.COMPLETE -> CompleteScreen(onSignOut = {
                store.clearSession()
                CookieManager.getInstance().removeAllCookies { CookieManager.getInstance().flush() }
                WebStorage.getInstance().deleteAllData()
                signedIn = false
            })
        }
    }
}
