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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PermissionSetup(notificationsEnabled: Boolean, onRefresh: () -> Unit, onContinue: () -> Unit) {
    val context = LocalContext.current
    var requested by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        requested = true
        onRefresh()
    }
    fun openSettings() {
        val primary = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        if (runCatching { context.startActivity(primary) }.isFailure && runCatching { context.startActivity(fallback) }.isFailure) {
            Toast.makeText(context, "Unable to open settings on this device.", Toast.LENGTH_LONG).show()
        }
    }
    SetupShell(step = 1, eyebrow = "LET’S GET YOU SET UP", title = "A little access.\nA lot of control.",
        description = "First, turn on notifications so you can see and control Vortex when it runs in the background.") {
        SetupCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = Lime)
                Text("Notifications", color = White, fontSize = 20.sp)
                Text(if (notificationsEnabled) "Notifications are enabled on this device." else
                    "A visible notification keeps you informed whenever a background activity is running.", color = Mist, lineHeight = 22.sp)
                StatusPill(if (notificationsEnabled) "ENABLED" else "NEEDS YOUR ACCESS", notificationsEnabled)
            }
        }
        Text("No notification reading, usage access, or storage access is needed for this step.", color = Mist, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 4.dp))
        if (notificationsEnabled) {
            PrimaryAction("Continue to Discord", onContinue)
        } else {
            if (requested) Text("Still off? Enable notifications in Android settings, then come back.", color = Lime, fontSize = 13.sp)
            if (Build.VERSION.SDK_INT >= 33 && !requested) {
                PrimaryAction("Allow notifications", { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) })
            } else {
                PrimaryAction("Open notification settings", ::openSettings)
            }
            if (Build.VERSION.SDK_INT >= 33 && !requested) SecondaryAction("Open settings instead", ::openSettings, Modifier.fillMaxWidth())
        }
    }
}
