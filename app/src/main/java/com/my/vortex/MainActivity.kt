package com.my.vortex

import android.Manifest
import android.animation.ValueAnimator
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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

    @Suppress("DEPRECATION")
    private fun usageAccessEnabled(): Boolean {
        val ops = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= 29) ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        else ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun batteryUnrestricted(): Boolean = getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)

    private fun openSettings(primary: Intent, fallback: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) {
        if (runCatching { startActivity(primary) }.isFailure && runCatching { startActivity(fallback) }.isFailure) {
            Toast.makeText(this, "Unable to open settings on this device.", Toast.LENGTH_LONG).show()
        }
    }

    private fun notificationSettings() = openSettings(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
    private fun usageSettings() = openSettings(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    private fun batterySettings() = openSettings(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))

    @Composable
    private fun VortexApp() {
        val store = remember { SessionStore(this) }
        var launched by rememberSaveable { mutableStateOf(false) }
        var enabled by remember { mutableStateOf(notificationsEnabled()) }
        var usageEnabled by remember { mutableStateOf(usageAccessEnabled()) }
        var batteryEnabled by remember { mutableStateOf(batteryUnrestricted()) }
        var acknowledged by remember { mutableStateOf(store.acknowledged) }
        var signedIn by remember { mutableStateOf(store.hasSession()) }
        var asked by rememberSaveable { mutableStateOf(false) }
        var manageAccess by rememberSaveable { mutableStateOf(false) }
        var movingToSignIn by rememberSaveable { mutableStateOf(false) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            asked = true
            enabled = notificationsEnabled()
        }
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    enabled = notificationsEnabled()
                    usageEnabled = usageAccessEnabled()
                    batteryEnabled = batteryUnrestricted()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        LaunchedEffect(Unit) {
            if (!launched) { delay(if (ValueAnimator.areAnimatorsEnabled()) 1200 else 200); launched = true }
        }
        LaunchedEffect(movingToSignIn, enabled) {
            if (movingToSignIn) {
                if (enabled && ValueAnimator.areAnimatorsEnabled()) delay(1050)
                movingToSignIn = false
            }
        }
        val screen = when {
            !launched -> "loading"
            movingToSignIn && enabled -> "accessComplete"
            manageAccess -> "permissions"
            else -> startupStage(enabled, acknowledged, signedIn).name
        }
        AnimatedContent(targetState = screen, transitionSpec = {
            if (ValueAnimator.areAnimatorsEnabled()) {
                (fadeIn(tween(360)) + slideInHorizontally(tween(420)) { it / 12 }) togetherWith
                    (fadeOut(tween(220)) + slideOutHorizontally(tween(300)) { -it / 16 })
            } else fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }, label = "setup transition") { target ->
            when (target) {
                "loading" -> LoadingScreen()
                "accessComplete" -> AccessCompleteScreen()
                "permissions", StartupStage.PERMISSIONS.name -> PermissionScreen(
                    enabled = enabled, asked = asked, usageEnabled = usageEnabled, batteryEnabled = batteryEnabled,
                    onAllow = {
                        if (Build.VERSION.SDK_INT >= 33 && !asked) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else notificationSettings()
                    }, onSettings = ::notificationSettings, onUsageSettings = ::usageSettings,
                    onBatterySettings = ::batterySettings,
                    onContinue = {
                        enabled = notificationsEnabled()
                        if (enabled) {
                            if (!manageAccess && !signedIn && ValueAnimator.areAnimatorsEnabled()) movingToSignIn = true
                            store.acknowledged = true
                            acknowledged = true
                            manageAccess = false
                        }
                    },
                    onBack = if (manageAccess && enabled) ({ manageAccess = false }) else null
                )
                StartupStage.SIGN_IN.name -> SignInScreen(onToken = { token ->
                    try { store.save(token); signedIn = true; true } catch (_: Exception) { false }
                })
                else -> CompleteScreen(onManageAccess = { manageAccess = true }, onSignOut = {
                    store.clearSession()
                    CookieManager.getInstance().removeAllCookies { CookieManager.getInstance().flush() }
                    WebStorage.getInstance().deleteAllData()
                    signedIn = false
                })
            }
        }
    }
}
