package com.my.vortex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.vortex.resources.R
import com.my.vortex.ui.components.OrbitMark
import com.my.vortex.ui.components.VortexInk
import com.my.vortex.ui.components.VortexMint

@Composable
internal fun LaunchScreen() {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(VortexInk, Color(0xFF203E40)))),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            OrbitMark(Modifier.size(170.dp))
            Text("VORTEX", color = Color.White, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, letterSpacing = 5.sp)
            Text(stringResource(R.string.launch_tagline), color = VortexMint, style = MaterialTheme.typography.bodyMedium)
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = VortexMint, strokeWidth = 2.dp)
        }
    }
}
