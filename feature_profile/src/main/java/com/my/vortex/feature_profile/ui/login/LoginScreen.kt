/*
 * Copyright (C) 2022 yzziK(Vaibhav). See the project license.
 */
package com.my.vortex.feature_profile.ui.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.my.vortex.resources.R

private const val LOGIN_URL = "https://vortexlogin.wispbyte.app:15748/login"

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var browserUnavailable by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.oauth_subtitle), style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Button(onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LOGIN_URL)).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                            })
                            browserUnavailable = false
                        } catch (_: ActivityNotFoundException) {
                            browserUnavailable = true
                        }
                    }) {
                        Text(stringResource(R.string.oauth_sign_in))
                    }
                    if (browserUnavailable) {
                        Text(stringResource(R.string.oauth_browser_unavailable),
                            color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
