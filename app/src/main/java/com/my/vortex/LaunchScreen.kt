package com.my.vortex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun LaunchScreen() {
    Box(Modifier.fillMaxSize().background(Night), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            VortexEmblem(Modifier.size(120.dp))
            Text("VORTEX", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp)
            Text("YOUR SPACE TO SHOW UP", color = Mist, fontSize = 10.sp, letterSpacing = 2.sp)
            CircularProgressIndicator(Modifier.padding(top = 20.dp).size(20.dp), color = Lime, strokeWidth = 2.dp)
        }
    }
}
