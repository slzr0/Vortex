package com.my.vortex

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.my.vortex.feature_profile.ui.login.LoginScreen
import com.my.vortex.preference.getLanguageConfig
import com.my.vortex.resources.R
import com.my.vortex.ui.theme.LocalDarkTheme
import com.my.vortex.ui.theme.LocalDynamicColorSwitch
import com.my.vortex.ui.theme.SettingsProvider
import com.my.vortex.ui.theme.VortexTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var usageAccessStatus: MutableState<Boolean>
    private lateinit var notificationListenerAccess: MutableState<Boolean>
    private lateinit var authStore: AuthStore
    private var handlingCallback by mutableStateOf(false)
    private var authGeneration by mutableIntStateOf(0)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authStore = AuthStore(applicationContext)
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
        handleOAuthIntent(intent)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            SettingsProvider(windowSizeClass.widthSizeClass) {
                VortexTheme(
                    darkTheme = LocalDarkTheme.current.isDarkTheme(),
                    isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    isDynamicColorEnabled = LocalDynamicColorSwitch.current,
                ) {
                    var retry by remember { mutableIntStateOf(0) }
                    key(retry) {
                        val session by authStore.session.collectAsState(initial = AuthState.Loading)
                        when (val current = if (handlingCallback) AuthState.Loading else session) {
                            AuthState.Loading -> Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) { CircularProgressIndicator() }
                            AuthState.SignedOut -> LoginScreen()
                            AuthState.ReadError -> Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(stringResource(R.string.oauth_storage_error))
                                Button(onClick = { retry++ }) { Text(stringResource(R.string.try_again)) }
                            }
                            is AuthState.SignedIn -> key(authGeneration) {
                                Vortex(
                                    usageAccessStatus = usageAccessStatus,
                                    notificationListenerAccess = notificationListenerAccess,
                                    user = current.user,
                                    onLogout = ::logout
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(incoming: Intent?) {
        if (incoming?.action != Intent.ACTION_VIEW || incoming.data == null) return
        val callback = OAuthCallback.fromIntent(incoming)
        if (callback == null) {
            // Clear sensitive callback data so a rotation does not process it repeatedly.
            setIntent(Intent(incoming).setData(null))
            Toast.makeText(this, R.string.oauth_invalid_callback, Toast.LENGTH_LONG).show()
            return
        }
        handlingCallback = true
        lifecycleScope.launch {
            try {
                authStore.save(callback)
                // Recreate the navigation graph at Home even when switching accounts.
                authGeneration++
                setIntent(Intent(incoming).setData(null))
            } catch (_: IOException) {
                setIntent(Intent(incoming).setData(null))
                Toast.makeText(this@MainActivity, R.string.oauth_storage_error, Toast.LENGTH_LONG).show()
            } finally {
                handlingCallback = false
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                authStore.clear()
            } catch (_: IOException) {
                Toast.makeText(this@MainActivity, R.string.oauth_storage_error, Toast.LENGTH_LONG).show()
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
