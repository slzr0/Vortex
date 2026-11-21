package com.my.vortex

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.my.vortex.preference.getLanguageConfig
import com.my.vortex.ui.theme.LocalDarkTheme
import com.my.vortex.ui.theme.LocalDynamicColorSwitch
import com.my.vortex.ui.theme.SettingsProvider
import com.my.vortex.ui.theme.VortexTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var usageAccessStatus: MutableState<Boolean>
    private lateinit var notificationListenerAccess: MutableState<Boolean>

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationContext.filesDir.resolve("datastore/vortex_auth.preferences_pb").delete()
        usageAccessStatus = mutableStateOf(hasUsageAccess())
        notificationListenerAccess = mutableStateOf(hasNotificationAccess())
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }
        runBlocking {
            if (Build.VERSION.SDK_INT < 33) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(getLanguageConfig()))
            }
        }
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            SettingsProvider(windowSizeClass.widthSizeClass) {
                VortexTheme(
                    darkTheme = LocalDarkTheme.current.isDarkTheme(),
                    isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    isDynamicColorEnabled = LocalDynamicColorSwitch.current,
                ) {
                    Vortex(
                        usageAccessStatus = usageAccessStatus,
                        notificationListenerAccess = notificationListenerAccess,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notificationListenerAccess.value = hasNotificationAccess()
        usageAccessStatus.value = hasUsageAccess()
    }

    @Suppress("DEPRECATION")
    private fun Context.hasUsageAccess(): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName
            ) == AppOpsManager.MODE_ALLOWED
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun Context.hasNotificationAccess(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val component = com.my.vortex.feature_rpc_base.services.notificationListenerComponent(this)
        return enabled?.split(':')?.any { android.content.ComponentName.unflattenFromString(it) == component } == true
    }

    companion object {
        fun setLanguage(locale: String) {
            val localeListCompat = if (locale.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(locale)
            AppCompatDelegate.setApplicationLocales(localeListCompat)
        }
    }
}
