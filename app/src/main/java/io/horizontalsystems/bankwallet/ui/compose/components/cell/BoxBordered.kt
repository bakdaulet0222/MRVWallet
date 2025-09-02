package com.mrv.wallet.ui.compose.components.cell

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mrv.wallet.ui.compose.components.HsDivider

@Composable
fun BoxBorderedTop(
    content: @Composable () -> Unit
) {
    Box {
        HsDivider(modifier = Modifier.align(Alignment.TopCenter))

        content()
    }
}
