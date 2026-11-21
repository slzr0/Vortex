/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * CustomScreenViewModel.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_custom_rpc

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.vortex.data.utils.uriToFile
import com.my.vortex.domain.model.rpc.RpcConfig
import com.my.vortex.domain.use_case.upload_galleryImage.UploadGalleryImageUseCase
import com.my.vortex.feature_custom_rpc.components.sheet.stringToData
import com.my.vortex.preference.Prefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class CustomScreenViewModel @Inject constructor(
    private val uploadGalleryImageUseCase: UploadGalleryImageUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
    private val uiEventChannel = Channel<UiEvent>(capacity = Channel.UNLIMITED)

    init {
        val lastConfig = Prefs[Prefs.LAST_RUN_CUSTOM_RPC, ""]
        val initialConfig = if (Prefs[Prefs.APPLY_FIELDS_FROM_LAST_RUN_RPC, false] && lastConfig.isNotBlank()) {
            lastConfig.stringToData()
        } else {
            RpcConfig(type = Prefs[Prefs.CUSTOM_ACTIVITY_TYPE, 0].toString(),
                status = Prefs[Prefs.CUSTOM_ACTIVITY_STATUS, "online"])
        }
        _uiState.value = _uiState.value.copy(rpcConfig = initialConfig)
        viewModelScope.launch {
            uiEventChannel.consumeAsFlow()
                .collect { event ->
                    processEvent(event)
                }
        }
    }

    private suspend fun uploadImage(uri: Uri, result: (String?) -> Unit) {
        val uploaded = withContext(Dispatchers.IO) {
            var file: File? = null
            try {
                file = context.uriToFile(uri)
                uploadGalleryImageUseCase(file)?.removePrefix("mp:")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            } finally {
                file?.delete()
            }
        }
        withContext(Dispatchers.Main) { result(uploaded) }
    }

    fun onEvent(event: UiEvent) {
        viewModelScope.launch {
            uiEventChannel.send(event)
        }
    }

    private suspend fun processEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SetFieldsFromConfig -> {
                _uiState.value = _uiState.value.copy(rpcConfig = event.config)
            }

            UiEvent.TriggerBottomSheet -> {
                _uiState.value =
                    _uiState.value.copy(showBottomSheet = !_uiState.value.showBottomSheet)
            }

            UiEvent.TriggerActivityTypeDropDownMenu -> {
                _uiState.value =
                    _uiState.value.copy(activityTypeIsExpanded = !_uiState.value.activityTypeIsExpanded)
            }

            UiEvent.TriggerStatusDropDownMenu -> {
                _uiState.value =
                    _uiState.value.copy(statusIsExpanded = !_uiState.value.statusIsExpanded)
            }

            UiEvent.TriggerPlatformDropDownMenu -> {
                _uiState.value =
                    _uiState.value.copy(platformIsExpanded = !_uiState.value.platformIsExpanded)
            }

            UiEvent.TriggerStartTimeStampsDialog -> {
                _uiState.value =
                    _uiState.value.copy(showStartTimeStampsPickerDialog = !_uiState.value.showStartTimeStampsPickerDialog)
            }

            UiEvent.TriggerStopTimeStampsDialog -> {
                _uiState.value =
                    _uiState.value.copy(showStopTimeStampsPickerDialog = !_uiState.value.showStopTimeStampsPickerDialog)
            }

            is UiEvent.UploadImage -> {
                viewModelScope.launch { uploadImage(event.uri, event.callback) }
            }

            is UiEvent.SetImage -> {
                val config = _uiState.value.rpcConfig
                _uiState.value = _uiState.value.copy(rpcConfig = if (event.large)
                    config.copy(largeImg = event.image) else config.copy(smallImg = event.image))
            }

            UiEvent.SheetEvent.TriggerStoragePermissionRequest -> {
                _uiState.value = _uiState.value.copy(
                    showStoragePermissionRequestDialog = !_uiState.value.showStoragePermissionRequestDialog,
                    showBottomSheet = false
                )
            }

            UiEvent.SheetEvent.TriggerDeleteDialog -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteDialog = !_uiState.value.showDeleteDialog,
                    showBottomSheet = false
                )
            }

            UiEvent.SheetEvent.TriggerLoadDialog -> {
                _uiState.value = _uiState.value.copy(
                    showLoadDialog = !_uiState.value.showLoadDialog,
                    showBottomSheet = false
                )
            }

            UiEvent.SheetEvent.TriggerShareDialog -> {
                _uiState.value = _uiState.value.copy(
                    showShareDialog = !_uiState.value.showShareDialog,
                    showBottomSheet = false
                )
            }

            UiEvent.SheetEvent.TriggerPreviewDialog -> {
                _uiState.value = _uiState.value.copy(
                    showPreviewDialog = !_uiState.value.showPreviewDialog,
                    showBottomSheet = false
                )
            }

            UiEvent.SheetEvent.TriggerSaveDialog -> {
                _uiState.value = _uiState.value.copy(
                    showSaveDialog = !_uiState.value.showSaveDialog,
                    showBottomSheet = false
                )
            }

            UiEvent.SheetEvent.ClearAllFields -> {
                _uiState.value = _uiState.value.copy(
                    rpcConfig = RpcConfig(),
                    showBottomSheet = false
                )
            }
        }
    }
}
