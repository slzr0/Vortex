/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UiEvents.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_custom_rpc

import com.my.vortex.domain.model.rpc.RpcConfig
import android.net.Uri

sealed interface UiEvent {
    object TriggerBottomSheet: UiEvent
    object TriggerActivityTypeDropDownMenu: UiEvent
    object TriggerStatusDropDownMenu: UiEvent
    object TriggerPlatformDropDownMenu: UiEvent
    class SetFieldsFromConfig(val config: RpcConfig): UiEvent
    object TriggerStartTimeStampsDialog: UiEvent
    object TriggerStopTimeStampsDialog: UiEvent
    class UploadImage(val uri: Uri, val callback: (result: String?) -> Unit): UiEvent
    class SetImage(val image: String, val large: Boolean): UiEvent

    sealed interface SheetEvent: UiEvent {
        object TriggerLoadDialog: SheetEvent
        object TriggerShareDialog: SheetEvent
        object TriggerSaveDialog: SheetEvent
        object TriggerDeleteDialog: SheetEvent
        object TriggerPreviewDialog: SheetEvent
        object TriggerStoragePermissionRequest: SheetEvent
        object ClearAllFields: SheetEvent
    }
}

