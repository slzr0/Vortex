/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppsRpc.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

@file:Suppress("DEPRECATION")

package com.my.vortex.feature_apps_rpc

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppsOutage
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.vortex.feature_rpc_base.AppUtils
import com.my.vortex.feature_rpc_base.services.AppDetectionService
import com.my.vortex.feature_rpc_base.services.CustomRpcService
import com.my.vortex.feature_rpc_base.services.ExperimentalRpc
import com.my.vortex.feature_rpc_base.services.MediaRpcService
import com.my.vortex.resources.R
import com.my.vortex.ui.components.AppsItem
import com.my.vortex.ui.components.BackButton
import com.my.vortex.ui.components.SearchBar
import com.my.vortex.ui.components.SwitchBar
import com.my.vortex.ui.components.preference.PreferencesHint

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsRPC(
    state: AppsState,
    updateAppEnabled: (String) -> Unit,
    onBackPressed: () -> Unit,
    hasUsageAccess: Boolean,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    val ctx = LocalContext.current
    var serviceEnabled by remember { mutableStateOf(AppUtils.appDetectionRunning()) }
    var searchText by remember { mutableStateOf("") }
    var isSearchBarVisible by remember { mutableStateOf(false) }
    val visibleApps = remember(state.apps, searchText) {
        state.apps.filter { it.name.contains(searchText, ignoreCase = true) || it.pkg.contains(searchText, ignoreCase = true) }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.main_appDetection),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }
                },
                navigationIcon = { BackButton { onBackPressed() } },
                actions = {
                    if (isSearchBarVisible) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            SearchBar(
                                text = searchText,
                                onTextChanged = { searchText = it },
                                onClose = { isSearchBarVisible = false; searchText = "" },
                                placeholder = stringResource(id = R.string.search_placeholder)
                            )
                        }
                    } else {
                        IconButton(onClick = { isSearchBarVisible = !isSearchBarVisible }) {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    item {
                        AnimatedVisibility(
                            visible = !hasUsageAccess
                        ) {
                            PreferencesHint(
                                title = stringResource(id = R.string.usage_access),
                                description = stringResource(id = R.string.usage_access_desc),
                                icon = Icons.Default.AppsOutage,
                            ) {
                                when (hasUsageAccess) {
                                    false -> ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                    else -> Unit
                                }
                            }
                        }
                    }
                    item {
                        SwitchBar(
                            title = stringResource(id = R.string.enable_appsRpc),
                            isChecked = serviceEnabled,
                            enabled = hasUsageAccess
                        ) {
                            serviceEnabled = !serviceEnabled
                            when (serviceEnabled) {
                                true -> {
                                    ctx.stopService(Intent(ctx, MediaRpcService::class.java))
                                    ctx.stopService(Intent(ctx, CustomRpcService::class.java))
                                    ctx.stopService(Intent(ctx, ExperimentalRpc::class.java))
                                    ctx.startService(Intent(ctx, AppDetectionService::class.java))
                                }

                                false -> ctx.stopService(
                                    Intent(
                                        ctx,
                                        AppDetectionService::class.java
                                    )
                                )
                            }
                        }
                    }
                    item {
                        Text(stringResource(R.string.choose_apps), modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (visibleApps.isEmpty()) {
                        item { Text(stringResource(R.string.no_apps_found), modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    items(visibleApps.size, key = { idx -> visibleApps[idx].pkg }) { i ->
                        val app = visibleApps[i]
                        AppsItem(name = app.name, pkg = app.pkg,
                            isChecked = state.enabledApps[app.pkg] ?: false,
                            onClick = { updateAppEnabled(app.pkg) })
                    }
                }
            }
        }
    }
}