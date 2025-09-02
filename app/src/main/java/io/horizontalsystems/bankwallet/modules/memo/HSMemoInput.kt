package com.mrv.wallet.modules.memo

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrv.wallet.R
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.FormsInput

@Composable
fun HSMemoInput(
    maxLength: Int,
    onValueChange: (String) -> Unit
) {
    FormsInput(
        modifier = Modifier.padding(horizontal = 16.dp),
        hint = stringResource(R.string.Send_DialogMemoHint),
        hintColor = ComposeAppTheme.colors.andy,
        hintStyle = ComposeAppTheme.typography.bodyItalic,
        textColor = ComposeAppTheme.colors.leah,
        textStyle = ComposeAppTheme.typography.bodyItalic,
        pasteEnabled = false,
        singleLine = true,
        maxLength = maxLength,
        onValueChange = onValueChange
    )
}
