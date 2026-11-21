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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.vortex.feature_profile.getUserInfo
import com.my.vortex.feature_profile.ui.component.DiscordLoginButton
import com.my.vortex.feature_profile.ui.component.DiscordLoginWebView
import com.my.vortex.preference.Prefs
import com.my.vortex.resources.R
import com.my.vortex.ui.components.glassScaffoldColor
import com.my.vortex.ui.components.glassSurfaceColor
import com.my.vortex.ui.components.vortexGlass
import com.my.vortex.ui.components.SetupProgress
import com.my.vortex.ui.components.VortexHero
import androidx.compose.ui.text.font.FontWeight
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
    Scaffold(containerColor = glassScaffoldColor()) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item { SetupProgress(step = 2) }
            item {
                VortexHero(stringResource(R.string.setup_step_two), stringResource(R.string.login_title),
                    stringResource(R.string.login_subtitle))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.setup_account_label), color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Surface(modifier = Modifier.vortexGlass(RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(12.dp))
                            }
                            Text(stringResource(R.string.login_privacy_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.login_privacy_description), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (error) Text(stringResource(R.string.login_failed), color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium)
                            if (verifying) {
                                CircularProgressIndicator()
                                Text(stringResource(R.string.login_verifying), style = MaterialTheme.typography.bodySmall)
                            } else DiscordLoginButton(onClick = { error = false; showSheet = true }, enabled = true)
                        }
                    }
                    Text(stringResource(R.string.setup_signin_note), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
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
