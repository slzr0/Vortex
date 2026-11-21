package com.my.vortex

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.my.vortex.resources.R
import com.my.vortex.ui.components.SetupProgress
import com.my.vortex.ui.components.glassScaffoldColor
import com.my.vortex.ui.components.glassSurfaceColor
import com.my.vortex.ui.components.vortexGlass
import com.my.vortex.ui.components.VortexHero

@Composable
internal fun PermissionSetup(onRefresh: () -> Unit) {
    val context = LocalContext.current
    var denied by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        onRefresh()
    }
    fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        if (runCatching { context.startActivity(intent) }.isFailure && runCatching { context.startActivity(fallback) }.isFailure) {
            Toast.makeText(context, R.string.setup_settings_unavailable, Toast.LENGTH_LONG).show()
        }
    }
    Scaffold(containerColor = glassScaffoldColor()) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item { SetupProgress(step = 1) }
            item {
                VortexHero(stringResource(R.string.setup_step_one), stringResource(R.string.setup_welcome),
                    stringResource(R.string.setup_intro))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.setup_permission_label), color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Surface(modifier = Modifier.vortexGlass(RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Icon(Icons.Outlined.NotificationsActive, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(12.dp).size(26.dp))
                            }
                            Text(stringResource(R.string.setup_notifications_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.setup_notifications_desc), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (denied) Text(stringResource(R.string.setup_permission_denied), color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                            if (Build.VERSION.SDK_INT >= 33) {
                                Button(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.setup_allow_notifications))
                                }
                            }
                            OutlinedButton(onClick = ::openNotificationSettings, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.setup_open_notification_settings))
                            }
                        }
                    }
                    Text(stringResource(R.string.setup_minimal_permissions), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
