/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * LoginScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_profile.ui.login

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
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.vortex.feature_profile.getUserInfo
import com.my.vortex.feature_profile.ui.component.DiscordLoginButton
import com.my.vortex.feature_profile.ui.component.DiscordLoginWebView
import com.my.vortex.preference.Prefs
import com.my.vortex.resources.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onCompleted: () -> Unit, onBackPressed: () -> Unit) {
    var showSheet by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Scaffold { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Surface(shape = RoundedCornerShape(30.dp), color = Color(0xFF171D3D)) {
                    Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF28235D), Color(0xFF10495C))))
                        .padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.setup_step_two), style = MaterialTheme.typography.labelLarge, color = Color(0xFF9DEAFF))
                        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Text(stringResource(R.string.login_subtitle), style = MaterialTheme.typography.bodyLarge, color = Color(0xFFD8E6F5))
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.login_privacy_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.login_privacy_description), style = MaterialTheme.typography.bodyMedium)
                        if (error) Text(stringResource(R.string.login_failed), color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium)
                        if (verifying) {
                            CircularProgressIndicator()
                            Text(stringResource(R.string.login_verifying), style = MaterialTheme.typography.bodySmall)
                        } else DiscordLoginButton(onClick = { error = false; showSheet = true }, enabled = true)
                    }
                }
            }
        }
    }
    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }) {
            DiscordLoginWebView { token ->
                if (!verifying) {
                    verifying = true
                    showSheet = false
                    scope.launch {
                        try {
                            val valid = token.isNotBlank() && getUserInfo(token)
                            if (valid) {
                                Prefs[Prefs.TOKEN] = token
                                onCompleted()
                            } else error = true
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            error = true
                        } finally {
                            verifying = false
                        }
                    }
                }
            }
        }
    }
}
