package com.mrv.wallet.modules.multiswap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mrv.wallet.R
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.BoxTyler44
import com.mrv.wallet.ui.compose.components.ButtonSecondary
import com.mrv.wallet.ui.compose.components.ButtonSecondaryCircle

@Composable
fun SuggestionsBar(
    modifier: Modifier = Modifier,
    percents: List<Int> = listOf(25, 50, 75, 100),
    onDelete: () -> Unit,
    onSelect: (Int) -> Unit,
    selectEnabled: Boolean,
    deleteEnabled: Boolean,
) {
    Box(modifier = modifier) {
        BoxTyler44(borderTop = true) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                percents.forEach { percent ->
                    ButtonSecondary(
                        enabled = selectEnabled,
                        onClick = { onSelect.invoke(percent) }
                    ) {
                        Text(
                            text = "$percent%",
                            modifier = modifier,
                            style = ComposeAppTheme.typography.captionSB,
                            color = if (selectEnabled) {
                                ComposeAppTheme.colors.leah
                            } else {
                                ComposeAppTheme.colors.andy
                            },
                        )
                    }
                }
                ButtonSecondaryCircle(
                    icon = R.drawable.ic_delete_20,
                    enabled = deleteEnabled,
                    tint = if (deleteEnabled) {
                        ComposeAppTheme.colors.leah
                    } else {
                        ComposeAppTheme.colors.andy
                    },
                    onClick = onDelete
                )
            }
        }
    }
}
