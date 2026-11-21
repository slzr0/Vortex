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

import android.annotation.SuppressLint
import android.content.Intent
import android.content.ComponentName
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.my.vortex.resources.R
import com.my.vortex.feature_settings.SettingsDrawer
import com.my.vortex.feature_rpc_base.services.VortexTileService
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.my.vortex.domain.model.toVersion
import com.my.vortex.feature_about.about.About
import com.my.vortex.feature_about.about.Credits
import com.my.vortex.feature_about.about.CreditsScreenViewModel
import com.my.vortex.feature_apps_rpc.AppsRPC
import com.my.vortex.feature_apps_rpc.AppsScreenViewModel
import com.my.vortex.feature_console_rpc.GamesScreen
import com.my.vortex.feature_console_rpc.GamesViewModel
import com.my.vortex.feature_custom_rpc.CustomRPC
import com.my.vortex.feature_custom_rpc.CustomScreenViewModel
import com.my.vortex.feature_home.Home
import com.my.vortex.feature_home.HomeScreenViewModel
import com.my.vortex.feature_home.feature.homeFeaturesProvider
import com.my.vortex.feature_logs.LogScreen
import com.my.vortex.feature_logs.LogsViewModel
import com.my.vortex.feature_media_rpc.MediaRPC
import com.my.vortex.feature_media_rpc.MediaScreenViewModel
import com.my.vortex.feature_profile.ui.login.LoginScreen
import com.my.vortex.feature_profile.ui.user.UserState
import com.my.vortex.feature_profile.ui.user.UserScreen
import com.my.vortex.feature_profile.ui.user.UserViewModel
import com.my.vortex.feature_rpc_base.AppUtils
import com.my.vortex.feature_rpc_base.services.AppDetectionService
import com.my.vortex.feature_rpc_base.services.CustomRpcService
import com.my.vortex.feature_rpc_base.services.ExperimentalRpc
import com.my.vortex.feature_rpc_base.services.MediaRpcService
import com.my.vortex.feature_settings.language.Language
import com.my.vortex.feature_settings.rpc_settings.RpcSettings
import com.my.vortex.feature_settings.style.Appearance
import com.my.vortex.feature_settings.style.DarkThemePreferences
import com.my.vortex.feature_startup.StartUp
import com.my.vortex.navigation.Routes
import com.my.vortex.navigation.animatedComposable
import com.my.vortex.preference.Prefs
import xyz.dead8309.feature_experimental_rpc.ExperimentalRpcScreen
import xyz.dead8309.feature_experimental_rpc.ExperimentalRpcViewmodel
import xyz.dead8309.feature_experimental_rpc.apps.ExperimentalRpcAppsScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ComponentActivity.Vortex(
    usageAccessStatus: MutableState<Boolean>,
    notificationListenerAccess: MutableState<Boolean>,
) {
    val navController = rememberAnimatedNavController()
    var user by remember { mutableStateOf(Prefs.getUser()) }
    var loggedIn by remember {
        mutableStateOf(Prefs[Prefs.TOKEN, ""].isNotEmpty() && Prefs[Prefs.USER_ID, ""].isNotEmpty())
    }
    var accountGeneration by remember { mutableIntStateOf(0) }
    val logout = {
        Prefs.remove(Prefs.TOKEN)
        Prefs.remove(Prefs.USER_DATA)
        Prefs.remove(Prefs.USER_ID)
        Prefs.remove(Prefs.USER_BIO)
        Prefs.remove(Prefs.USER_NITRO)
        user = null
        loggedIn = false
        accountGeneration++
    }
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val tabs = listOf(
        Triple(Routes.HOME, R.string.nav_home, Icons.Outlined.Home),
        Triple(Routes.APPS_DETECTION, R.string.nav_apps, Icons.Outlined.Apps),
        Triple(Routes.MEDIA_RPC, R.string.nav_media, Icons.Outlined.MusicNote),
        Triple(Routes.CUSTOM_RPC, R.string.nav_custom, Icons.Outlined.Edit),
        Triple(Routes.SETTINGS, R.string.settings, Icons.Outlined.Settings),
    )
    Scaffold(bottomBar = {
        if (tabs.any { it.first == currentRoute }) {
            NavigationBar {
                tabs.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = route == currentRoute,
                        onClick = {
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME)
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(stringResource(label), maxLines = 1) }
                    )
                }
            }
        }
    }) { padding ->
        AnimatedNavHost(
            modifier = Modifier.fillMaxSize().padding(padding),
            navController = navController,
            startDestination = Routes.HOME
        ) {
            animatedComposable(Routes.SETUP) {
                StartUp(
                    usageAccessStatus = usageAccessStatus,
                    mediaControlStatus = notificationListenerAccess,
                    navigateToLanguages = {
                        navController.navigate(Routes.LANGUAGES) {
                            launchSingleTop = true
                        }
                    },
                    navigateToHome = {
                        Prefs[Prefs.IS_FIRST_LAUNCHED] = false
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    navigateToLogin = {
                        navController.navigate(Routes.PROFILE)
                    })
            }
            animatedComposable(Routes.HOME) {
                val release = Prefs.getSavedLatestRelease()
                val viewModel by viewModels<HomeScreenViewModel>()
                val state = viewModel.aboutScreenState.collectAsState().value
                val showBadge = release
                    ?.toVersion()
                    ?.whetherNeedUpdate(BuildConfig.VERSION_NAME.toVersion())
                    ?: false
                Home(
                    state = state,
                    checkForUpdates = {
                        if (release != null && release.toVersion() > BuildConfig.VERSION_NAME.toVersion()) {
                            viewModel.setReleaseFromPrefs(release)
                        } else {
                            viewModel.getLatestUpdate()
                        }
                    },
                    showBadge = showBadge,
                    onLogout = logout,
                    navigateToSetup = { navController.navigate(Routes.SETUP) },
                    features = homeFeaturesProvider(
                        navigateTo = { navController.navigate(it) },
                        hasUsageAccess = usageAccessStatus,
                        hasNotificationAccess = notificationListenerAccess,
                        userVerified = user?.verified == true
                    ),
                    user = user,
                    navigateToProfile = {
                        navController.navigate(Routes.PROFILE)
                    },
                    navigateToStyleAndAppearance = {
                        navController.navigate(Routes.STYLE_AND_APPEARANCE)
                    },
                    navigateToLanguages = {
                        navController.navigate(Routes.LANGUAGES)
                    },
                    navigateToAbout = {
                        navController.navigate(Routes.ABOUT)
                    },
                    navigateToRpcSettings = {
                        navController.navigate(Routes.RPC_SETTINGS)
                    },
                    navigateToLogsScreen = {
                        navController.navigate(Routes.LOGS_SCREEN)
                    }
                )
            }
            animatedComposable(Routes.SETTINGS) {
                Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }) { innerPadding ->
                    SettingsDrawer(
                        modifier = Modifier.fillMaxWidth().padding(innerPadding),
                        user = user,
                        showVortexQuickieRequestItem = !VortexTileService.tileAdded.value,
                        onRequestAddTile = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val manager = getSystemService(android.app.StatusBarManager::class.java)
                                manager.requestAddTileService(
                                    ComponentName(this@Vortex, VortexTileService::class.java),
                                    getString(R.string.qs_tile_label),
                                    android.graphics.drawable.Icon.createWithResource(this@Vortex, R.drawable.ic_tile_play),
                                    {} ) {}
                            }
                        },
                        navigateToProfile = { navController.navigate(Routes.PROFILE) },
                        navigateToStyleAndAppearance = { navController.navigate(Routes.STYLE_AND_APPEARANCE) },
                        navigateToLanguages = { navController.navigate(Routes.LANGUAGES) },
                        navigateToAbout = { navController.navigate(Routes.ABOUT) },
                        navigateToRpcSettings = { navController.navigate(Routes.RPC_SETTINGS) },
                        navigateToLogsScreen = { navController.navigate(Routes.LOGS_SCREEN) },
                        navigateToSetup = { navController.navigate(Routes.SETUP) }
                    )
                }
            }
            animatedComposable(Routes.APPS_DETECTION) {
                val viewModel by viewModels<AppsScreenViewModel>()
                AppsRPC(
                    onBackPressed = { navController.popBackStack() },
                    hasUsageAccess = usageAccessStatus.value,
                    state = viewModel.state.collectAsState().value,
                    updateAppEnabled = viewModel::updateAppEnabled,
                )
            }
            animatedComposable(Routes.CUSTOM_RPC) {
                val viewModel by viewModels<CustomScreenViewModel>()
                CustomRPC(
                    onBackPressed = { navController.popBackStack() },
                    state = viewModel.uiState.collectAsState().value,
                    onEvent = viewModel::onEvent
                )
            }
            animatedComposable(Routes.MEDIA_RPC) {
                val viewModel by viewModels<MediaScreenViewModel>()
                MediaRPC(
                    onBackPressed = { navController.popBackStack() },
                    state = viewModel.state.collectAsState().value,
                    hasNotificationAccess = notificationListenerAccess.value,
                    updateMediaAppEnabled = viewModel::updateMediaAppEnabled
                )
            }
            animatedComposable(Routes.PROFILE) {
                if (loggedIn) {
                    val viewModel: UserViewModel = viewModel(
                        key = "profile-$accountGeneration",
                        factory = this@Vortex.defaultViewModelProviderFactory
                    )
                    val state = viewModel.state.value
                    LaunchedEffect(state) {
                        if (state is UserState.LoadingCompleted) user = state.user
                    }
                    UserScreen(
                        state = state,
                        onBackPressed = { navController.popBackStack() },
                        onLoggedOut = logout
                    )
                } else {
                    LoginScreen(
                        onCompleted = {
                            accountGeneration++
                            loggedIn = true
                        },
                        onBackPressed = { navController.popBackStack() }
                    )
                }
            }
            animatedComposable(Routes.CONSOLE_RPC) {
                val viewModel by viewModels<GamesViewModel>()
                GamesScreen(
                    onBackPressed = { navController.popBackStack() },
                    onEvent = { viewModel.onUiEvent(it) },
                    isSearchBarVisible = viewModel.isSearchBarVisible.value,
                    state = viewModel.state.value,
                    serviceEnabled = AppUtils.customRpcRunning()
                )
            }
            animatedComposable(Routes.LANGUAGES) {
                Language(
                    onBackPressed = { navController.popBackStack() },
                    updateLocaleLanguage = MainActivity::setLanguage
                )
            }
            animatedComposable(Routes.STYLE_AND_APPEARANCE) {
                Appearance(onBackPressed = {
                    navController.popBackStack()
                }) {
                    navController.navigate(Routes.DARK_THEME)
                }
            }
            animatedComposable(Routes.DARK_THEME) {
                DarkThemePreferences {
                    navController.popBackStack()
                }
            }
            animatedComposable(Routes.RPC_SETTINGS) {
                RpcSettings { navController.popBackStack() }
            }
            animatedComposable(Routes.LOGS_SCREEN) {
                val viewModel by viewModels<LogsViewModel>()
                LogScreen(viewModel, onBackPressed = { navController.popBackStack() })
            }
            animatedComposable(Routes.ABOUT) {
                About(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    navigateToCredits = {
                        navController.navigate(Routes.CREDITS)
                    }
                )
            }
            animatedComposable(Routes.CREDITS) {
                val viewModel by viewModels<CreditsScreenViewModel>()
                Credits(
                    state = viewModel.creditScreenState.collectAsState().value,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }

            val experimentalRpcViewModel by lazy {
                viewModels<ExperimentalRpcViewmodel>()
            }

            animatedComposable(Routes.EXPERIMENTAL_RPC) {
                ExperimentalRpcScreen(
                    state = experimentalRpcViewModel.value.uiState.collectAsState().value,
                    onEvent = experimentalRpcViewModel.value::onEvent,
                    onBackPressed = { navController.popBackStack() },
                    hasUsageAccess = usageAccessStatus.value,
                    hasNotificationAccess = notificationListenerAccess.value,
                    navigateToAppSelection = {
                        navController.navigate(Routes.EXPERIMENTAL_RPC_APPS)
                    },
                )
            }

            animatedComposable(Routes.EXPERIMENTAL_RPC_APPS) {
                ExperimentalRpcAppsScreen(
                    onBackPressed = { navController.popBackStack() },
                    state = experimentalRpcViewModel.value.uiState.collectAsState().value,
                    onEvent = experimentalRpcViewModel.value::onEvent,
                )
            }
        }
    }
}