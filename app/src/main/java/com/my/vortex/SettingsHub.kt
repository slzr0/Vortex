package com.my.vortex

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.vortex.preference.Prefs
import com.my.vortex.preference.setGlassStyle
import com.my.vortex.preference.setGlassIntensity
import com.my.vortex.ui.theme.LocalGlassStyle
import com.my.vortex.ui.theme.LocalGlassIntensity
import com.my.vortex.resources.R
import com.my.vortex.ui.components.VortexHero
import com.my.vortex.ui.components.VortexMint
import com.my.vortex.ui.components.glassScaffoldColor
import com.my.vortex.ui.components.glassSurfaceColor
import com.my.vortex.ui.components.vortexGlass

@Composable
internal fun SettingsHub(
    onAppearance: () -> Unit,
    onLanguage: () -> Unit,
    onPresenceSettings: () -> Unit,
    onLogs: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    var showLogout by remember { mutableStateOf(false) }
    var showClear by remember { mutableStateOf(false) }
    val accountName = remember { runCatching { Prefs.getUser() }.getOrNull()?.let { it.globalName ?: it.username } }
    val glassStyle = LocalGlassStyle.current
    val glassIntensity = LocalGlassIntensity.current
    var restoreCustom by remember { mutableStateOf(Prefs[Prefs.APPLY_FIELDS_FROM_LAST_RUN_RPC, false]) }
    fun openSystemSettings(intent: Intent) {
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        if (runCatching { context.startActivity(intent) }.isFailure && runCatching { context.startActivity(fallback) }.isFailure) {
            Toast.makeText(context, R.string.setup_settings_unavailable, Toast.LENGTH_SHORT).show()
        }
    }
    Scaffold(containerColor = glassScaffoldColor()) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                VortexHero(stringResource(R.string.settings_eyebrow), stringResource(R.string.settings_title_new),
                    stringResource(R.string.settings_intro_new)) {
                    if (!accountName.isNullOrBlank()) Text(stringResource(R.string.settings_connected_as, accountName),
                        style = MaterialTheme.typography.labelMedium, color = VortexMint)
                }
            }

            item { SettingsSection(stringResource(R.string.settings_personalize)) }
            item {
                Surface(modifier = Modifier.vortexGlass(RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp),
                    color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(10.dp).size(22.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.settings_glass_title), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.settings_glass_description), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = glassStyle, onCheckedChange = ::setGlassStyle)
                        }
                        if (glassStyle) {
                            Text(stringResource(R.string.settings_glass_strength), style = MaterialTheme.typography.labelMedium)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                itemsIndexed(listOf(R.string.settings_glass_soft, R.string.settings_glass_balanced, R.string.settings_glass_vivid)) { index, label ->
                                    FilterChip(selected = glassIntensity == index, onClick = { setGlassIntensity(index) },
                                        label = { Text(stringResource(label), maxLines = 1) })
                                }
                            }
                        }
                        Text(stringResource(R.string.settings_glass_live), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { SettingsAction(Icons.Outlined.Palette, stringResource(R.string.display), stringResource(R.string.settings_appearance_desc), onAppearance) }
            item { SettingsAction(Icons.Outlined.Language, stringResource(R.string.language), stringResource(R.string.settings_language_desc), onLanguage) }
            item { SettingsAction(Icons.Outlined.Tune, stringResource(R.string.settings_presence), stringResource(R.string.settings_presence_desc), onPresenceSettings) }
            item {
                Surface(modifier = Modifier.vortexGlass(RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_restore_custom), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.settings_restore_custom_desc), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = restoreCustom, onCheckedChange = {
                            restoreCustom = it
                            Prefs[Prefs.APPLY_FIELDS_FROM_LAST_RUN_RPC] = it
                        })
                    }
                }
            }
            item { SettingsSection(stringResource(R.string.settings_reliability)) }
            item { SettingsAction(Icons.Outlined.BatteryFull, stringResource(R.string.console_battery_settings), stringResource(R.string.settings_battery_desc)) {
                openSystemSettings(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } }
            item { SettingsAction(Icons.Outlined.Notifications, stringResource(R.string.console_notification_settings), stringResource(R.string.settings_notifications_desc)) {
                openSystemSettings(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            } }
            item { SettingsAction(Icons.Outlined.BugReport, stringResource(R.string.logs), stringResource(R.string.settings_logs_desc), onLogs) }
            item { SettingsAction(Icons.Outlined.DeleteOutline, stringResource(R.string.settings_clear_cache), stringResource(R.string.settings_clear_cache_desc)) { showClear = true } }
            item { SettingsSection(stringResource(R.string.settings_account)) }
            item { SettingsAction(Icons.Outlined.Info, stringResource(R.string.about), stringResource(R.string.settings_about_desc), onAbout) }
            item { SettingsAction(Icons.Outlined.Logout, stringResource(R.string.logout), stringResource(R.string.settings_logout_desc)) { showLogout = true } }
            item { Text(stringResource(R.string.settings_privacy_note), modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        }
    }
    if (showClear) AlertDialog(onDismissRequest = { showClear = false }, title = { Text(stringResource(R.string.settings_clear_cache)) },
        text = { Text(stringResource(R.string.settings_clear_confirm)) },
        confirmButton = { TextButton(onClick = {
            Prefs.remove(Prefs.SAVED_IMAGES)
            Prefs.remove(Prefs.SAVED_ARTWORK)
            showClear = false
            Toast.makeText(context, R.string.settings_cache_cleared, Toast.LENGTH_SHORT).show()
        }) { Text(stringResource(R.string.settings_clear)) } },
        dismissButton = { TextButton(onClick = { showClear = false }) { Text(stringResource(R.string.cancel)) } })
    if (showLogout) AlertDialog(onDismissRequest = { showLogout = false }, title = { Text(stringResource(R.string.logout)) },
        text = { Text(stringResource(R.string.settings_logout_confirm)) },
        confirmButton = { TextButton(onClick = { showLogout = false; onLogout() }) { Text(stringResource(R.string.logout)) } },
        dismissButton = { TextButton(onClick = { showLogout = false }) { Text(stringResource(R.string.cancel)) } })
}

@Composable
private fun SettingsSection(title: String) {
    Text(title, modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 2.dp),
        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsAction(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.vortexGlass(RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
