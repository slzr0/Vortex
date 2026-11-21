package com.my.vortex.feature_home.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.vortex.resources.R

@Composable
fun Features(homeItems: List<HomeFeature> = emptyList(), onValueUpdate: (Int) -> Unit) {
    val uriHandler = LocalUriHandler.current
    var info by remember { mutableStateOf<HomeFeature?>(null) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        homeItems.forEachIndexed { index, item ->
            val colors = MaterialTheme.colorScheme
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (item.isChecked) colors.primaryContainer else colors.surfaceVariant.copy(alpha = 0.42f),
                border = BorderStroke(1.dp, if (item.isChecked) colors.primary.copy(alpha = 0.35f) else colors.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .then(if (item.route != null) Modifier.clickable { item.onClick(item.route) } else Modifier)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = RoundedCornerShape(14.dp), color = colors.surface.copy(alpha = 0.7f)) {
                        Icon(painterResource(item.icon), contentDescription = null,
                            tint = colors.primary, modifier = Modifier.padding(12.dp).size(24.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        if (item.tooltipText.isNotBlank()) {
                            Text(item.tooltipText, style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant, maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                        if (item.showSwitch) {
                            Text(stringResource(if (item.isChecked) R.string.android_on else R.string.android_off),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.isChecked) colors.primary else colors.onSurfaceVariant)
                        }
                    }
                    if (item.tooltipText.isNotBlank()) {
                        IconButton(onClick = { info = item }) {
                            Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.learn_more))
                        }
                    }
                    if (item.showSwitch) {
                        Switch(checked = item.isChecked, onCheckedChange = {
                            item.onCheckedChange(it)
                            onValueUpdate(index)
                        })
                    }
                }
            }
        }
    }
    info?.let { item ->
        AlertDialog(
            onDismissRequest = { info = null },
            title = { Text(item.title) },
            text = { Text(item.tooltipText) },
            confirmButton = {
                TextButton(onClick = { info = null; uriHandler.openUri(item.featureDocsLink) }) {
                    Text(stringResource(R.string.learn_more))
                }
            },
            dismissButton = { TextButton(onClick = { info = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
