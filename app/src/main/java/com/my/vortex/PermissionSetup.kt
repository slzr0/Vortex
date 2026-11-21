package com.my.vortex

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.vortex.resources.R

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
        if (runCatching { context.startActivity(intent) }.isFailure) {
            Toast.makeText(context, R.string.setup_settings_unavailable, Toast.LENGTH_LONG).show()
        }
    }
    Scaffold { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Surface(shape = RoundedCornerShape(30.dp), color = Color(0xFF171D3D)) {
                    Column(Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF28235D), Color(0xFF10495C))))
                        .padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.setup_step_one), color = Color(0xFF9DEAFF), style = MaterialTheme.typography.labelLarge)
                        Text(stringResource(R.string.setup_welcome), color = Color.White, style = MaterialTheme.typography.headlineLarge)
                        Text(stringResource(R.string.setup_intro), color = Color(0xFFD8E6F5), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.setup_notifications_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.setup_notifications_desc), style = MaterialTheme.typography.bodyMedium)
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
            }
            item {
                Text(stringResource(R.string.setup_minimal_permissions), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
