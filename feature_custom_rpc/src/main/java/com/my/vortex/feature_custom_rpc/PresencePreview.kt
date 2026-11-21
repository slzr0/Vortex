package com.my.vortex.feature_custom_rpc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.my.vortex.domain.model.rpc.RpcConfig
import com.my.vortex.resources.R
import com.my.vortex.ui.components.glassSurfaceColor
import com.my.vortex.ui.components.vortexGlass
import kotlinx.coroutines.delay

@Composable
internal fun PresencePreview(config: RpcConfig, modifier: Modifier = Modifier) {
    var artwork by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(config.largeImg) {
        artwork = null
        val candidate = previewArtworkUrl(config.largeImg)
        if (candidate != null) {
            delay(650)
            artwork = candidate
        }
    }
    Surface(modifier = modifier.fillMaxWidth().vortexGlass(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.custom_preview_label), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.custom_preview_draft), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(76.dp).clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    if (artwork != null) AsyncImage(model = artwork, contentDescription = null,
                        modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(config.name.ifBlank { stringResource(R.string.custom_preview_empty) },
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (config.details.isNotBlank()) Text(config.details, style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (config.state.isNotBlank()) Text(config.state, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            val buttonLabels = listOf(config.button1, config.button2).filter { it.isNotBlank() }
            if (buttonLabels.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    buttonLabels.forEach { label ->
                        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            Text(stringResource(R.string.custom_preview_hint), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
