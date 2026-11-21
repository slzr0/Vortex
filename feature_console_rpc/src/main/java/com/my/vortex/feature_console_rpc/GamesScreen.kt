/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * GamesScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_console_rpc

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.my.vortex.domain.model.Game
import com.my.vortex.domain.model.rpc.RpcConfig
import com.my.vortex.feature_rpc_base.AppUtils
import com.my.vortex.feature_rpc_base.services.AppDetectionService
import com.my.vortex.feature_rpc_base.services.CustomRpcService
import com.my.vortex.feature_rpc_base.services.ExperimentalRpc
import com.my.vortex.feature_rpc_base.services.MediaRpcService
import com.my.vortex.preference.Prefs
import com.my.vortex.resources.R
import com.my.vortex.ui.components.BackButton
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onBackPressed: () -> Unit,
    onEvent: (UiEvent) -> Unit,
    state: GamesState,
    serviceEnabled: Boolean,
    isSearchBarVisible: Boolean,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val json = remember { Json { ignoreUnknownKeys = true } }
    var selected by remember {
        val saved = Prefs[Prefs.LAST_RUN_CONSOLE_RPC, ""]
        val config = runCatching { json.decodeFromString<RpcConfig>(saved) }.getOrNull()
        mutableStateOf(config?.toConsoleGame())
    }
    var running by remember { mutableStateOf(serviceEnabled) }
    var query by remember { mutableStateOf("") }
    var showManual by remember { mutableStateOf(false) }
    var showFavorites by rememberSaveable { mutableStateOf(false) }
    var selectedPlatform by rememberSaveable { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf(Prefs[Prefs.CONSOLE_STATUS, "dnd"].takeIf { it in CONSOLE_STATUSES } ?: "dnd") }
    var showElapsed by remember { mutableStateOf(Prefs[Prefs.CONSOLE_SHOW_ELAPSED, true]) }
    var favorites by remember {
        val saved = Prefs[Prefs.CONSOLE_FAVORITES, "[]"]
        mutableStateOf(runCatching { json.decodeFromString<List<RpcConfig>>(saved) }
            .getOrDefault(emptyList()).mapNotNull { it.toConsoleGame() })
    }
    val powerManager = remember(context) { context.getSystemService(PowerManager::class.java) }
    val notificationManager = remember(context) { context.getSystemService(NotificationManager::class.java) }
    var batteryExempt by remember { mutableStateOf(powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true) }
    var notificationsEnabled by remember { mutableStateOf(notificationManager?.areNotificationsEnabled() == true) }

    fun toggleFavorite(game: Game) {
        favorites = if (favorites.any { it.sameConsoleGame(game) }) {
            favorites.filterNot { it.sameConsoleGame(game) }
        } else {
            favorites + game
        }
        Prefs[Prefs.CONSOLE_FAVORITES] = json.encodeToString(favorites.map { it.toConsoleConfig(0) })
    }

    fun openSystemSettings(intent: Intent) {
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        if (runCatching { context.startActivity(intent) }.isFailure &&
            runCatching { context.startActivity(fallback) }.isFailure) {
            Toast.makeText(context, R.string.console_settings_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                running = AppUtils.customRpcRunning() && Prefs[Prefs.ACTIVE_CUSTOM_RPC_MODE, ""] == "console"
                batteryExempt = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
                notificationsEnabled = notificationManager?.areNotificationsEnabled() == true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun startPresence() {
        val game = selected ?: return
        if (Prefs[Prefs.TOKEN, ""].isBlank()) {
            Toast.makeText(context, R.string.console_sign_in_required, Toast.LENGTH_LONG).show()
            return
        }
        val previous = runCatching { json.decodeFromString<RpcConfig>(Prefs[Prefs.LAST_RUN_CONSOLE_RPC, ""]) }.getOrNull()
        val isSameServiceRunning = AppUtils.customRpcRunning() && Prefs[Prefs.ACTIVE_CUSTOM_RPC_MODE, ""] == "console"
        val startTime = game.consoleStartTime(previous, isSameServiceRunning, System.currentTimeMillis())
        val config = json.encodeToString(game.toConsoleConfig(startTime, status, showElapsed))
        val result = runCatching {
            context.stopService(Intent(context, AppDetectionService::class.java))
            context.stopService(Intent(context, MediaRpcService::class.java))
            context.stopService(Intent(context, ExperimentalRpc::class.java))
            context.stopService(Intent(context, CustomRpcService::class.java))
            val service = Intent(context, CustomRpcService::class.java).putExtra("RPC", config)
            checkNotNull(context.startService(service))
        }
        if (result.isSuccess) {
            Prefs[Prefs.LAST_RUN_CONSOLE_RPC] = config
            Prefs[Prefs.ACTIVE_CUSTOM_RPC_MODE] = "console"
            running = true
        } else {
            running = false
            Toast.makeText(context, R.string.console_start_failed, Toast.LENGTH_LONG).show()
        }
    }

    val availableGames = remember(showFavorites, favorites, query, state) {
        if (showFavorites) filterConsoleGames(favorites, query) else (state as? GamesState.Success)?.games.orEmpty()
    }
    val platforms = remember(availableGames, selectedPlatform) {
        (availableGames.map { it.platform } + listOfNotNull(selectedPlatform)).distinct().sortedBy { it.lowercase() }
    }
    val visibleGames = remember(availableGames, selectedPlatform) { filterByPlatform(availableGames, selectedPlatform) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.main_consoleRpc)) },
                navigationIcon = { BackButton { onBackPressed() } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.console_hero_title), style = MaterialTheme.typography.headlineSmall)
                        Text(stringResource(R.string.console_hero_description), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            selected?.let { "${it.game_title} · ${it.platform}" }
                                ?: stringResource(R.string.console_no_selection),
                            style = MaterialTheme.typography.titleMedium
                        )
                        selected?.let { game ->
                            TextButton(onClick = { toggleFavorite(game) }) {
                                Text(stringResource(if (favorites.any { it.sameConsoleGame(game) }) R.string.console_remove_favorite else R.string.console_add_favorite))
                            }
                        }
                        Text(
                            stringResource(if (running) R.string.console_service_running else R.string.console_service_stopped),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { startPresence() }, enabled = selected != null) {
                                Text(stringResource(if (running) R.string.console_update else R.string.console_start))
                            }
                            if (running) {
                                OutlinedButton(onClick = {
                                    if (Prefs[Prefs.ACTIVE_CUSTOM_RPC_MODE, ""] == "console") {
                                        context.stopService(Intent(context, CustomRpcService::class.java))
                                    }
                                    running = false
                                }) { Text(stringResource(R.string.console_stop)) }
                            }
                        }
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.console_presence_options), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.console_status_hint), style = MaterialTheme.typography.bodySmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CONSOLE_STATUSES) { value ->
                                val label = when (value) {
                                    "online" -> R.string.status_online
                                    "idle" -> R.string.status_idle
                                    else -> R.string.status_dnd
                                }
                                FilterChip(selected = status == value, onClick = {
                                    status = value
                                    Prefs[Prefs.CONSOLE_STATUS] = value
                                }, label = { Text(stringResource(label)) })
                            }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.console_show_elapsed), modifier = Modifier.weight(1f))
                            Switch(checked = showElapsed, onCheckedChange = {
                                showElapsed = it
                                Prefs[Prefs.CONSOLE_SHOW_ELAPSED] = it
                            })
                        }
                        Text(stringResource(R.string.console_apply_options), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.console_background_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(if (batteryExempt) R.string.console_battery_exempt else R.string.console_battery_optimized),
                            style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(if (notificationsEnabled) R.string.console_notifications_on else R.string.console_notifications_off),
                            style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.console_background_hint), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(modifier = Modifier.weight(1f), onClick = { openSystemSettings(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }) {
                                Text(stringResource(R.string.console_battery_settings))
                            }
                            TextButton(modifier = Modifier.weight(1f), onClick = {
                                openSystemSettings(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                            }) { Text(stringResource(R.string.console_notification_settings)) }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; onEvent(UiEvent.Search(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.console_search)) },
                    shape = RoundedCornerShape(18.dp)
                )
            }
            item {
                OutlinedButton(onClick = { showManual = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.console_manual))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !showFavorites, onClick = { showFavorites = false; selectedPlatform = null },
                        label = { Text(stringResource(R.string.console_all_games)) })
                    FilterChip(selected = showFavorites, onClick = { showFavorites = true; selectedPlatform = null },
                        label = { Text(stringResource(R.string.console_favorites)) })
                }
            }
            if (platforms.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(selected = selectedPlatform == null, onClick = { selectedPlatform = null },
                                label = { Text(stringResource(R.string.console_all_platforms)) })
                        }
                        items(platforms) { platform ->
                            FilterChip(selected = selectedPlatform == platform, onClick = { selectedPlatform = platform },
                                label = { Text(platform) })
                        }
                    }
                }
            }
            if (showFavorites || state is GamesState.Success) {
                item {
                    Text(
                        if (visibleGames.isEmpty()) stringResource(if (showFavorites && favorites.isEmpty()) R.string.console_empty_favorites else R.string.console_empty)
                        else stringResource(R.string.console_results, visibleGames.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(visibleGames) { game ->
                    ConsoleGameCard(
                        game = game,
                        selected = selected?.sameConsoleGame(game) == true,
                        favorite = favorites.any { it.sameConsoleGame(game) },
                        onFavorite = { toggleFavorite(game) },
                        onClick = { selected = game }
                    )
                }
            } else when (state) {
                GamesState.Loading -> item {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
                is GamesState.Error -> item {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.console_catalog_error), style = MaterialTheme.typography.titleMedium)
                        Text(state.error, style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { onEvent(UiEvent.TryAgain) }) { Text(stringResource(R.string.try_again)) }
                    }
                }
                else -> Unit
            }
        }
    }

    if (showManual) {
        var title by remember { mutableStateOf("") }
        var platform by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showManual = false },
            title = { Text(stringResource(R.string.console_manual)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.console_manual_description))
                    OutlinedTextField(value = title, onValueChange = { title = it.take(128) }, singleLine = true,
                        label = { Text(stringResource(R.string.console_game_title)) })
                    OutlinedTextField(value = platform, onValueChange = { platform = it.take(128) }, singleLine = true,
                        label = { Text(stringResource(R.string.console_platform)) })
                }
            },
            confirmButton = {
                TextButton(enabled = title.isNotBlank() && platform.isNotBlank(), onClick = {
                    selected = Game(platform.trim(), "", null, title.trim())
                    showManual = false
                }) { Text(stringResource(R.string.console_use_preset)) }
            },
            dismissButton = { TextButton(onClick = { showManual = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConsoleGameCard(game: Game, selected: Boolean, favorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AsyncImage(model = game.large_image, contentDescription = null,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)))
            Column(Modifier.weight(1f)) {
                Text(game.game_title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(game.platform, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) Icon(Icons.Outlined.Check, contentDescription = stringResource(R.string.console_selected))
            IconButton(onClick = onFavorite) {
                Icon(if (favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(if (favorite) R.string.console_remove_favorite else R.string.console_add_favorite))
            }
        }
    }
}
