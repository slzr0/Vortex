/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcSettings.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_settings.rpc_settings

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.my.vortex.data.rpc.Constants
import com.my.vortex.domain.model.rpc.RpcButtons
import com.my.vortex.preference.Prefs
import com.my.vortex.resources.R
import com.my.vortex.ui.components.BackButton
import com.my.vortex.ui.components.RpcField
import com.my.vortex.ui.components.SettingItem
import com.my.vortex.ui.components.Subtitle
import com.my.vortex.ui.components.dialog.SingleChoiceItem
import com.my.vortex.ui.components.preference.PreferenceSwitch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RpcSettings(onBackPressed: () -> Boolean) {
    val context = LocalContext.current
    var useImgur by remember { mutableStateOf(Prefs[Prefs.USE_IMGUR, false]) }
    var configsDir by remember { mutableStateOf(Prefs[Prefs.CONFIGS_DIRECTORY, ""]) }
    var showDirConfigDialog by remember { mutableStateOf(false) }
    var customActivityStatus by remember {
        mutableStateOf(Prefs[Prefs.CUSTOM_ACTIVITY_STATUS, "online"])
    }
    var customActivityType by remember {
        mutableStateOf(Prefs[Prefs.CUSTOM_ACTIVITY_TYPE, 0].toString())
    }
    var showActivityTypeDialog by remember {
        mutableStateOf(false)
    }
    var showActivityStatusDialog by remember {
        mutableStateOf(false)
    }
    var showApplicationIdDialog by remember {
        mutableStateOf(false)
    }
    var showImgurClientIdDialog by remember {
        mutableStateOf(false)
    }
    var setLastRunRpcConfigOption by remember {
        mutableStateOf(Prefs[Prefs.APPLY_FIELDS_FROM_LAST_RUN_RPC, false])
    }
    var customApplicationId by remember { mutableStateOf(Prefs[Prefs.CUSTOM_ACTIVITY_APPLICATION_ID, ""]) }
    var imgurClientId by remember { mutableStateOf(Prefs[Prefs.IMGUR_CLIENT_ID, Constants.IMGUR_CLIENT_ID]) }

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        LargeTopAppBar(title = {
            Text(
                text = stringResource(id = R.string.settings_presence),
                style = MaterialTheme.typography.headlineLarge,
            )
        }, navigationIcon = { BackButton { onBackPressed() } })
    }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp), color = Color(0xFF171D3D)) {
                    Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF28235D), Color(0xFF10495C))))
                        .padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_presence), style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text(stringResource(R.string.settings_presence_help), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD8E6F5))
                    }
                }
            }
            item {
                Subtitle(text = stringResource(R.string.general_settings))
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                item {
                    SettingItem(
                        title = stringResource(id = R.string.configs_directory),
                        description = configsDir.ifEmpty { stringResource(id = R.string.custom_rpc_directory) },
                        icon = Icons.Default.Storage,
                    ) {
                        showDirConfigDialog = true
                    }
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.custom_activity_type),
                    description = stringResource(id = R.string.settings_default_type_desc),
                    icon = Icons.Default.Code
                ) {
                    showActivityTypeDialog = true
                }

            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.custom_activity_status),
                    description = stringResource(id = R.string.settings_default_status_desc),
                    icon = Icons.Default.DoNotDisturbOn
                ) {
                    showActivityStatusDialog = true
                }
            }
            item {
                PreferenceSwitch(
                    title = stringResource(R.string.set_previous_run_config_in_custom_rpc),
                    description = stringResource(R.string.set_previous_run_config_in_custom_rpc_desc),
                    icon = Icons.Default.Cached,
                    isChecked = setLastRunRpcConfigOption
                ) {
                    setLastRunRpcConfigOption = !setLastRunRpcConfigOption
                    Prefs[Prefs.APPLY_FIELDS_FROM_LAST_RUN_RPC] = setLastRunRpcConfigOption
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.custom_application_id),
                    description = stringResource(id = R.string.custom_application_id_desc),
                    icon = Icons.Default.Pin
                ) {
                    showApplicationIdDialog = true
                }

            }
            item {
                Subtitle(text = stringResource(id = R.string.advance_settings))
            }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.use_imgur),
                    description = stringResource(id = R.string.use_imgur_desc),
                    icon = Icons.Default.Image,
                    isChecked = useImgur
                ) {
                    useImgur = !useImgur
                    Prefs[Prefs.USE_IMGUR] = useImgur
                }
            }
            item {
                AnimatedVisibility(visible = useImgur) {
                    SettingItem(
                        title = stringResource(id = R.string.set_imgur_client_id),
                        description = stringResource(id = R.string.set_imgur_client_id_desc),
                        icon = Icons.Default.Code
                    ) {
                        showImgurClientIdDialog = true
                    }
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.delete_saved_icon_urls),
                    description = stringResource(id = R.string.delete_saved_icon_urls_desc),
                    icon = Icons.Default.DeleteForever
                ) {
                    Prefs.remove(Prefs.SAVED_IMAGES)
                    Prefs.remove(Prefs.SAVED_ARTWORK)
                    Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (showDirConfigDialog) {
            AlertDialog(
                onDismissRequest = { showDirConfigDialog = false },
                confirmButton = {},
                icon = {
                    Icon(
                        imageVector = Icons.Default.Storage, contentDescription = null
                    )
                },
                title = { Text(stringResource(R.string.select_directory)) },
                text = {
                    Column {
                        SingleChoiceItem(
                            text = Constants.APP_DIRECTORY,
                            selected = configsDir == Constants.APP_DIRECTORY
                        ) {
                            configsDir = Constants.APP_DIRECTORY
                            Prefs[Prefs.CONFIGS_DIRECTORY] = configsDir
                            showDirConfigDialog = false
                        }
                        SingleChoiceItem(
                            text = Constants.DOWNLOADS_DIRECTORY,
                            selected = configsDir == Constants.DOWNLOADS_DIRECTORY
                        ) {
                            configsDir = Constants.DOWNLOADS_DIRECTORY
                            Prefs[Prefs.CONFIGS_DIRECTORY] = configsDir
                            showDirConfigDialog = false
                        }
                    }
                })
        }
        if (showActivityTypeDialog) {
            AlertDialog(
                onDismissRequest = {
                    showActivityTypeDialog = false
                },
                text = {
                    var activityTypeisExpanded by remember {
                        mutableStateOf(false)
                    }
                    val icon =
                        if (activityTypeisExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        }
                    RpcField(
                        value = customActivityType,
                        label = R.string.activity_type,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    activityTypeisExpanded = !activityTypeisExpanded
                                })
                        }) {
                        customActivityType = it
                    }
                    DropdownMenu(
                        expanded = activityTypeisExpanded, onDismissRequest = {
                            activityTypeisExpanded = !activityTypeisExpanded
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Constants.ACTIVITY_TYPE.forEach { (label, value) ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = label)
                                },
                                onClick = {
                                    customActivityType = value.toString()
                                    activityTypeisExpanded = false
                                },
                            )
                        }
                    }

                },
                confirmButton = {
                    TextButton(onClick = {
                        val type = customActivityType.toIntOrNull()
                        if (type != null && type in 0..5) {
                            Prefs[Prefs.CUSTOM_ACTIVITY_TYPE] = type
                            showActivityTypeDialog = false
                        }
                    }) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            )
        }

        if (showActivityStatusDialog) {
            AlertDialog(
                onDismissRequest = {
                    showActivityStatusDialog = false
                },
                confirmButton = {},
                text = {

                    Column {
                        Constants.ACTIVITY_STATUS.forEach { (resId, value) ->
                            SingleChoiceItem(
                                text = stringResource(resId),
                                selected = value == customActivityStatus
                            ) {
                                customActivityStatus = value
                                Prefs[Prefs.CUSTOM_ACTIVITY_STATUS] = value
                                showActivityStatusDialog = false
                            }
                        }
                    }
                }
            )
        }

        if (showApplicationIdDialog) {
            AlertDialog(
                onDismissRequest = {
                    showApplicationIdDialog = false
                },
                title = { Text("Application ID") },
                text = {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        RpcField(
                            value = customApplicationId,
                            label = R.string.application_id,
                            isError = customApplicationId.isNotEmpty() && (customApplicationId.length !in Constants.MAX_APPLICATION_ID_LENGTH_RANGE || !customApplicationId.all { it.isDigit() }),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            onValueChange = { newText ->
                                if (newText.length <= Constants.MAX_APPLICATION_ID_LENGTH_RANGE.last && newText.all { it.isDigit() }) {
                                    customApplicationId = newText
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (customApplicationId.isNotEmpty() && (customApplicationId.length !in Constants.MAX_APPLICATION_ID_LENGTH_RANGE || !customApplicationId.all { it.isDigit() })) {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid Application ID",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Prefs[Prefs.CUSTOM_ACTIVITY_APPLICATION_ID] = customApplicationId
                                showApplicationIdDialog = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
            )
        }

        if (showImgurClientIdDialog) {
            AlertDialog(
                onDismissRequest = {
                    imgurClientId = Prefs[Prefs.IMGUR_CLIENT_ID, Constants.IMGUR_CLIENT_ID]
                    showImgurClientIdDialog = false
                },
                title = { Text(stringResource(R.string.set_imgur_client_id)) },
                text = {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        RpcField(
                            value = imgurClientId,
                            label = R.string.imgur_client_id,
                            onValueChange = { newText ->
                                imgurClientId = newText
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (imgurClientId.isBlank()) {
                                imgurClientId = Constants.IMGUR_CLIENT_ID
                            }
                            Prefs[Prefs.IMGUR_CLIENT_ID] = imgurClientId
                            showImgurClientIdDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
            )
        }
    }
}