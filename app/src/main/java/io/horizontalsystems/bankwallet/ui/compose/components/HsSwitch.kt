package com.mrv.wallet.ui.compose.components

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.mrv.wallet.ui.compose.Bright
import com.mrv.wallet.ui.compose.ComposeAppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HsSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
){
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Switch(
            modifier = modifier,
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Bright,
                uncheckedThumbColor = Bright,
                checkedTrackColor = ComposeAppTheme.colors.jacob,
                uncheckedTrackColor = ComposeAppTheme.colors.andy,
                checkedTrackAlpha = 1f,
                uncheckedTrackAlpha = 1f,
            ),
            enabled = enabled
        )
    }
}
