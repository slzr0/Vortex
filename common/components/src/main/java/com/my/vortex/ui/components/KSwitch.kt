package com.my.vortex.ui.components

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun KSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enable: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Switch(
        modifier = modifier,
        checked = checked,
        enabled = enable,
        onCheckedChange = onClick?.let { callback -> { _: Boolean -> callback() } }
    )
}
