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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.my.vortex.feature_about.about.About
import com.my.vortex.feature_about.about.Credits
import com.my.vortex.feature_about.about.CreditsScreenViewModel
import com.my.vortex.feature_console_rpc.GamesScreen
import com.my.vortex.feature_console_rpc.GamesViewModel
import com.my.vortex.feature_custom_rpc.CustomRPC
import com.my.vortex.feature_custom_rpc.CustomScreenViewModel
import com.my.vortex.feature_logs.LogScreen
import com.my.vortex.feature_logs.LogsViewModel
import com.my.vortex.feature_profile.ui.login.LoginScreen
import com.my.vortex.feature_rpc_base.AppUtils
import com.my.vortex.feature_rpc_base.services.CustomRpcService
import com.my.vortex.feature_settings.language.Language
import com.my.vortex.feature_settings.rpc_settings.RpcSettings
import com.my.vortex.feature_settings.style.Appearance
import com.my.vortex.feature_settings.style.DarkThemePreferences
import com.my.vortex.navigation.Routes
import com.my.vortex.navigation.animatedComposable
import com.my.vortex.preference.Prefs
import com.my.vortex.resources.R
import com.my.vortex.ui.components.glassScaffoldColor
import com.my.vortex.ui.components.glassSurfaceColor
import com.my.vortex.ui.components.vortexGlass
import kotlinx.coroutines.delay

internal fun ComponentActivity.notificationsReady(): Boolean {
    val runtimeGranted = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
        this, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
    return runtimeGranted && getSystemService(NotificationManager::class.java)?.areNotificationsEnabled() == true
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ComponentActivity.Vortex() {
    var launchComplete by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!launchComplete) { delay(900); launchComplete = true }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionsReady by remember { mutableStateOf(notificationsReady()) }
    var signedIn by remember {
        mutableStateOf(Prefs[Prefs.TOKEN, ""].isNotBlank() && Prefs[Prefs.USER_ID, ""].isNotBlank())
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionsReady = notificationsReady()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    if (!launchComplete) {
        LaunchScreen()
        return
    }
    if (startupStage(permissionsReady, signedIn) == StartupStage.PERMISSIONS) {
        PermissionSetup(onRefresh = { permissionsReady = notificationsReady() })
        return
    }
    if (startupStage(permissionsReady, signedIn) == StartupStage.SIGN_IN) {
        LoginScreen(
            onCompleted = { signedIn = Prefs[Prefs.TOKEN, ""].isNotBlank() && Prefs[Prefs.USER_ID, ""].isNotBlank() },
            onBackPressed = { finish() }
        )
        return
    }

    val navController = rememberAnimatedNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val tabs = listOf(
        Triple(Routes.CONSOLE_RPC, R.string.nav_home, Icons.Outlined.SportsEsports),
        Triple(Routes.CUSTOM_RPC, R.string.nav_custom, Icons.Outlined.Edit),
        Triple(Routes.SETTINGS, R.string.settings, Icons.Outlined.Settings)
    )
    val logout: () -> Unit = {
        stopService(Intent(this, CustomRpcService::class.java))
        listOf(Prefs.TOKEN, Prefs.USER_DATA, Prefs.USER_ID, Prefs.USER_BIO, Prefs.USER_NITRO, Prefs.ACTIVE_CUSTOM_RPC_MODE)
            .forEach(Prefs::remove)
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        signedIn = false
    }
    Scaffold(containerColor = glassScaffoldColor(), bottomBar = {
        if (tabs.any { it.first == route }) {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)
                    .vortexGlass(RoundedCornerShape(26.dp)).clip(RoundedCornerShape(26.dp)),
                windowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceVariant),
                tonalElevation = 0.dp
            ) {
                tabs.forEach { (destination, title, icon) ->
                    NavigationBarItem(
                        selected = destination == route,
                        onClick = {
                            if (route != destination) navController.navigate(destination) {
                                popUpTo(Routes.CONSOLE_RPC) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(stringResource(title), maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }) { padding ->
        AnimatedNavHost(navController = navController, startDestination = Routes.CONSOLE_RPC,
            modifier = Modifier.fillMaxSize().padding(padding)) {
            animatedComposable(Routes.CONSOLE_RPC) { backStackEntry ->
                val model: GamesViewModel = viewModel(viewModelStoreOwner = backStackEntry,
                    factory = this@Vortex.defaultViewModelProviderFactory)
                GamesScreen(onBackPressed = { finish() }, onEvent = model::onUiEvent,
                    isSearchBarVisible = model.isSearchBarVisible.value, state = model.state.value,
                    serviceEnabled = AppUtils.customRpcRunning() && Prefs[Prefs.ACTIVE_CUSTOM_RPC_MODE, ""] == "console")
            }
            animatedComposable(Routes.CUSTOM_RPC) { backStackEntry ->
                val model: CustomScreenViewModel = viewModel(viewModelStoreOwner = backStackEntry,
                    factory = this@Vortex.defaultViewModelProviderFactory)
                CustomRPC(onBackPressed = { navController.popBackStack() },
                    state = model.uiState.collectAsState().value, onEvent = model::onEvent)
            }
            animatedComposable(Routes.SETTINGS) {
                SettingsHub(onAppearance = { navController.navigate(Routes.STYLE_AND_APPEARANCE) },
                    onLanguage = { navController.navigate(Routes.LANGUAGES) },
                    onPresenceSettings = { navController.navigate(Routes.RPC_SETTINGS) },
                    onLogs = { navController.navigate(Routes.LOGS_SCREEN) },
                    onAbout = { navController.navigate(Routes.ABOUT) }, onLogout = logout)
            }
            animatedComposable(Routes.LANGUAGES) {
                Language(onBackPressed = { navController.popBackStack() }, updateLocaleLanguage = MainActivity::setLanguage)
            }
            animatedComposable(Routes.STYLE_AND_APPEARANCE) {
                Appearance(onBackPressed = { navController.popBackStack() }) { navController.navigate(Routes.DARK_THEME) }
            }
            animatedComposable(Routes.DARK_THEME) { DarkThemePreferences { navController.popBackStack() } }
            animatedComposable(Routes.RPC_SETTINGS) { RpcSettings { navController.popBackStack() } }
            animatedComposable(Routes.LOGS_SCREEN) { backStackEntry ->
                val model: LogsViewModel = viewModel(viewModelStoreOwner = backStackEntry,
                    factory = this@Vortex.defaultViewModelProviderFactory)
                LogScreen(model, onBackPressed = { navController.popBackStack() })
            }
            animatedComposable(Routes.ABOUT) {
                About(onBackPressed = { navController.popBackStack() }, navigateToCredits = { navController.navigate(Routes.CREDITS) })
            }
            animatedComposable(Routes.CREDITS) { backStackEntry ->
                val model: CreditsScreenViewModel = viewModel(viewModelStoreOwner = backStackEntry,
                    factory = this@Vortex.defaultViewModelProviderFactory)
                Credits(state = model.creditScreenState.collectAsState().value, onBackPressed = { navController.popBackStack() })
            }
        }
    }
}
