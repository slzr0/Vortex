/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * Home.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_home

import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.my.vortex.domain.model.toVersion
import com.my.vortex.domain.model.user.User
import com.my.vortex.feature_home.feature.Features
import com.my.vortex.feature_home.feature.HomeFeature
import com.my.vortex.feature_home.feature.ToolTipContent
import com.my.vortex.feature_rpc_base.services.VortexTileService
import com.my.vortex.feature_settings.SettingsDrawer
import com.my.vortex.navigation.Routes
import com.my.vortex.resources.R
import com.my.vortex.ui.components.UpdateDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    state: HomeScreenState,
    checkForUpdates: () -> Unit,
    showBadge: Boolean,
    features: List<HomeFeature>,
    user: User?,
    navigateToProfile: () -> Unit,
    navigateToStyleAndAppearance: () -> Unit,
    navigateToLanguages: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToRpcSettings: () -> Unit,
    navigateToLogsScreen: () -> Unit,
    onLogout: () -> Unit = {},
    navigateToSetup: (() -> Unit)? = null,
) {
    val ctx = LocalContext.current
    var homeItems by remember {
        mutableStateOf(features)
    }
    var showUpdateDialog by remember {
        mutableStateOf(false)
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true })
    val isCollapsed = scrollBehavior.state.collapsedFraction > 0.55f

    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            homeItems = features
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                SettingsDrawer(
                    user = user,
                    showVortexQuickieRequestItem = !VortexTileService.tileAdded.value,
                    onRequestAddTile = {
                        val cn = ComponentName(ctx, VortexTileService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val sbm = ctx.getSystemService(android.app.StatusBarManager::class.java)
                            sbm.requestAddTileService(
                                    cn,
                                    ctx.getString(R.string.qs_tile_label),
                                    android.graphics.drawable.Icon.createWithResource(ctx, R.drawable.ic_tile_play),
                                    {}
                            ) {}
                        }
                    },
                    navigateToProfile = navigateToProfile,
                    navigateToStyleAndAppearance = navigateToStyleAndAppearance,
                    navigateToLanguages = navigateToLanguages,
                    navigateToAbout = navigateToAbout,
                    navigateToRpcSettings = navigateToRpcSettings,
                    navigateToLogsScreen = navigateToLogsScreen,
                    navigateToSetup = navigateToSetup
                )
            }
        }) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.welcome) + ", ${user?.globalName ?: user?.username ?: ""}",
                            style = if (isCollapsed) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineLarge,
                            maxLines = if (isCollapsed) 1 else Int.MAX_VALUE,
                            overflow = if (isCollapsed) androidx.compose.ui.text.style.TextOverflow.Ellipsis else androidx.compose.ui.text.style.TextOverflow.Clip,
                            modifier = Modifier.padding(end = if (isCollapsed) 0.dp else 12.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                        ) {
                            Icon(
                                Icons.Outlined.Menu, Icons.Outlined.Menu.name,
                            )
                        }
                    },
                    actions = {
                        if (showBadge) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        modifier = Modifier
                                            .offset(8.dp, (-14).dp)
                                            .size(8.dp)
                                            .clip(CircleShape),
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Update,
                                    contentDescription = "Update",
                                    modifier = Modifier.clickable {
                                        Toast.makeText(
                                            ctx,
                                            ctx.getString(R.string.update_check_for_update),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        checkForUpdates()
                                        showUpdateDialog = true
                                    }
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Update,
                                contentDescription = "Update",
                                modifier = Modifier.clickable {
                                    Toast.makeText(
                                        ctx,
                                        ctx.getString(R.string.update_check_for_update),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    checkForUpdates()
                                    showUpdateDialog = true
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { navigateToProfile() }) {
                            if (user != null) {
                                AsyncImage(
                                    model = user.avatar?.let { user.getAvatarImage() },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            CircleShape,
                                        )
                                        .clip(CircleShape),
                                    placeholder = painterResource(R.drawable.error_avatar),
                                    error = painterResource(R.drawable.error_avatar),
                                    fallback = painterResource(R.drawable.error_avatar),
                                    contentDescription = user.username
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = Icons.Default.Person.name,
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFF201C4B)
                    ) {
                        Column(
                            Modifier.background(Brush.linearGradient(listOf(Color(0xFF25205B), Color(0xFF124B61))))
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("V O R T E X", style = MaterialTheme.typography.labelMedium, color = Color(0xFF9BEAFF))
                            Text(stringResource(R.string.presence_overview), style = MaterialTheme.typography.headlineSmall,
                                color = Color.White)
                            Text(stringResource(R.string.presence_overview_desc), style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFDDE8FF))
                            Text(
                                homeItems.firstOrNull { it.isChecked }?.let { "${it.title} · ${stringResource(R.string.android_on)}" }
                                    ?: stringResource(R.string.home_no_active_mode),
                                style = MaterialTheme.typography.labelLarge, color = Color(0xFFA8EFFF)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(onClick = {
                                    homeItems.firstOrNull { it.route == Routes.CONSOLE_RPC }?.let { it.onClick(Routes.CONSOLE_RPC) }
                                }) { Text(stringResource(R.string.home_open_console)) }
                                if (user == null) {
                                    TextButton(onClick = navigateToProfile) {
                                        Text(stringResource(R.string.home_connect), color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    if (user != null) {
                        TextButton(onClick = onLogout, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(stringResource(R.string.logout))
                        }
                    }
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.presence_modes), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.presence_mode_hint), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    Features(homeItems) {
                        homeItems = homeItems.mapIndexed { j, item ->
                            if (it == j) {
                                item.copy(isChecked = !item.isChecked)
                            } else {
                                if (item.isChecked) {
                                    item.copy(isChecked = false)
                                } else {
                                    item
                                }
                            }
                        }
                    }
                }
            }
            when (state) {
                is HomeScreenState.LoadingCompleted -> {
                    if (showUpdateDialog) {
                        if (state.release.toVersion()
                                .whetherNeedUpdate(BuildConfig.VERSION_NAME.toVersion())
                        ) {
                            with(state.release) {
                                UpdateDialog(
                                    newVersionPublishDate = publishedAt ?: "",
                                    newVersionSize = assets?.getOrNull(0)?.size ?: 0,
                                    newVersionLog = body ?: "",
                                    onDismissRequest = {
                                        showUpdateDialog = false
                                    },
                                )
                            }
                        } else {
                            Toast.makeText(
                                ctx,
                                ctx.getString(R.string.update_no_updates_available),
                                Toast.LENGTH_SHORT
                            ).show()
                            showUpdateDialog = false
                        }
                    }
                }

                is HomeScreenState.Error -> {
                    if (showUpdateDialog) {
                        Toast.makeText(ctx, state.error ?: ctx.getString(R.string.unknown_error), Toast.LENGTH_LONG).show()
                        showUpdateDialog = false
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    Home(
        state = HomeScreenState.Loading,
        checkForUpdates = {},
        showBadge = true,
        features = fakeFeatures,
        user = fakeUser,
        navigateToProfile = { },
        navigateToStyleAndAppearance = { },
        navigateToLanguages = { },
        navigateToAbout = { },
        navigateToRpcSettings = { },
        navigateToLogsScreen = { }
    )
}

val fakeFeatures = listOf(
    HomeFeature(
        title = "App Detection",
        icon = R.drawable.ic_apps,
        shape = RoundedCornerShape(20.dp, 44.dp, 20.dp, 44.dp),
        tooltipText = ToolTipContent.APP_DETECTION_DOCS
    ), HomeFeature(
        title = "Media RPC",
        icon = R.drawable.ic_media_rpc,
        shape = RoundedCornerShape(44.dp, 20.dp, 44.dp, 20.dp),
        tooltipText = ToolTipContent.MEDIA_RPC_DOCS
    ), HomeFeature(
        title = "Custom RPC",
        icon = R.drawable.ic_rpc_placeholder,
        shape = RoundedCornerShape(44.dp, 20.dp, 44.dp, 20.dp),
        tooltipText = ToolTipContent.CUSTOM_RPC_DOCS
    ), HomeFeature(
        title = "Console RPC",
        icon = R.drawable.ic_console_games,
        shape = RoundedCornerShape(20.dp, 44.dp, 20.dp, 44.dp),
        tooltipText = ToolTipContent.CONSOLE_RPC_DOCS
    ),
    HomeFeature(
        title = "Experimental RPC",
        icon = R.drawable.ic_dev_rpc,
        shape = RoundedCornerShape(20.dp, 44.dp, 20.dp, 44.dp),
        tooltipText = ToolTipContent.EXPERIMENTAL_RPC_DOCS
    ),
    HomeFeature(
        title = "Coming Soon",
        icon = R.drawable.ic_info,
        shape = RoundedCornerShape(20.dp, 44.dp, 20.dp, 44.dp),
        showSwitch = false
    )
)

val fakeUser = User(
    accentColor = null,
    avatar = null,
    avatarDecoration = null,
    badges = null,
    banner = null,
    bannerColor = null,
    discriminator = "3050",
    id = null,
    publicFlags = null,
    username = "yzziK",
    special = null,
    verified = false,
    nitro = true,
    bio = "Hello 👋"
)