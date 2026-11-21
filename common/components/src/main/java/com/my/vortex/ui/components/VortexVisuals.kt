package com.my.vortex.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.my.vortex.resources.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val VortexInk = Color(0xFF101C2C)
val VortexMint = Color(0xFFB6F5D5)
val VortexPeach = Color(0xFFFFBEA1)

@Composable
fun OrbitMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val side = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(Color.White.copy(alpha = .08f), radius = side * .45f, center = center)
        drawCircle(VortexMint.copy(alpha = .3f), radius = side * .35f, center = center, style = Stroke(side * .008f))
        drawArc(VortexMint, 210f, 220f, false,
            topLeft = Offset(center.x - side * .33f, center.y - side * .33f),
            size = Size(side * .66f, side * .66f), style = Stroke(side * .025f, cap = StrokeCap.Round))
        drawArc(VortexPeach, 35f, 200f, false,
            topLeft = Offset(center.x - side * .23f, center.y - side * .23f),
            size = Size(side * .46f, side * .46f), style = Stroke(side * .025f, cap = StrokeCap.Round))
        drawCircle(Color.White, radius = side * .05f, center = center)
        drawCircle(VortexPeach, radius = side * .035f, center = Offset(center.x + side * .28f, center.y - side * .18f))
    }
}

@Composable
fun VortexHero(eyebrow: String, title: String, description: String, modifier: Modifier = Modifier,
               content: @Composable () -> Unit = {}) {
    Surface(modifier = modifier.fillMaxWidth().vortexGlass(RoundedCornerShape(28.dp)), shape = RoundedCornerShape(28.dp), color = VortexInk) {
        Box(Modifier.background(Brush.linearGradient(listOf(VortexInk, Color(0xFF1B3941), Color(0xFF243A3B))))) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(brush = Brush.radialGradient(listOf(VortexMint.copy(alpha = .16f), Color.Transparent),
                    center = Offset(size.width * .92f, size.height * .12f), radius = size.minDimension * .8f),
                    radius = size.minDimension * .8f, center = Offset(size.width * .92f, size.height * .12f))
                drawCircle(VortexPeach.copy(alpha = .08f), radius = size.minDimension * .46f,
                    center = Offset(size.width * .95f, size.height * .94f), style = Stroke(1.dp.toPx()))
            }
            OrbitMark(Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp).size(125.dp).alpha(.68f))
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(eyebrow.uppercase(), color = VortexMint, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Text(title, modifier = Modifier.fillMaxWidth(.82f), color = Color.White,
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(description, modifier = Modifier.fillMaxWidth(.86f), color = Color(0xFFD3E2DF),
                    style = MaterialTheme.typography.bodyMedium)
                content()
            }
        }
    }
}

@Composable
fun SetupProgress(step: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(2) { index ->
                Box(Modifier.weight(1f).height(4.dp).background(
                    if (index < step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(50)))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.setup_progress_permissions), modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.setup_progress_account), modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                color = if (step >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun VortexStatusPill(active: Boolean, label: String) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .12f)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(if (active) VortexMint else VortexPeach, RoundedCornerShape(50)))
            Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun glassBackground(): Brush {
    val colors = MaterialTheme.colorScheme
    val intensity = com.my.vortex.ui.theme.LocalGlassIntensity.current.coerceIn(0, 2)
    val highContrast = com.my.vortex.ui.theme.LocalDarkTheme.current.let { it.isHighContrastModeEnabled && it.isDarkTheme() }
    val blend = if (highContrast) 0f else listOf(.08f, .15f, .22f)[intensity]
    return Brush.linearGradient(listOf(
        androidx.compose.ui.graphics.lerp(colors.background, colors.primaryContainer, blend),
        colors.background,
        androidx.compose.ui.graphics.lerp(colors.background, colors.tertiaryContainer, blend)
    ))
}

@Composable
fun glassScaffoldColor(): Color = if (com.my.vortex.ui.theme.LocalGlassStyle.current)
    Color.Transparent else MaterialTheme.colorScheme.background

@Composable
fun glassSurfaceColor(fallback: Color): Color = if (com.my.vortex.ui.theme.LocalGlassStyle.current)
    Color.Transparent else fallback

@Composable
fun Modifier.vortexGlass(shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp)): Modifier {
    if (!com.my.vortex.ui.theme.LocalGlassStyle.current) return this
    val colors = MaterialTheme.colorScheme
    val intensity = com.my.vortex.ui.theme.LocalGlassIntensity.current.coerceIn(0, 2)
    val highContrast = com.my.vortex.ui.theme.LocalDarkTheme.current.let { it.isHighContrastModeEnabled && it.isDarkTheme() }
    val tint = if (highContrast) 0f else listOf(.06f, .12f, .19f)[intensity]
    val highlight = androidx.compose.ui.graphics.lerp(colors.surface, colors.primaryContainer, tint)
    return this
        .background(Brush.linearGradient(listOf(highlight.copy(alpha = if (highContrast) 1f else .94f), colors.surface.copy(alpha = if (highContrast) 1f else .80f))), shape)
        .then(Modifier.border(1.dp, Brush.linearGradient(listOf(
            colors.outlineVariant.copy(alpha = .85f),
            colors.primary.copy(alpha = if (highContrast) .8f else .18f),
            colors.outlineVariant.copy(alpha = .55f)
        )), shape))
}
