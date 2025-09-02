package com.mrv.wallet.modules.multiswap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mrv.wallet.R
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.cell.CellUniversal
import com.mrv.wallet.ui.compose.components.subhead2_grey
import com.mrv.wallet.ui.compose.components.subhead2_leah

@Composable
fun QuoteInfoRow(
    borderTop: Boolean = false,
    title: @Composable() (RowScope.() -> Unit),
    value: @Composable() (RowScope.() -> Unit),
) {
    CellUniversal(borderTop = borderTop) {
        title.invoke(this)
        HSpacer(width = 16.dp)
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
            value.invoke(this)
        }
    }
}

@Preview
@Composable
fun QuoteInfoRowPreview() {
    ComposeAppTheme {
        QuoteInfoRow(
            title = {
                subhead2_grey(text = stringResource(R.string.Swap_Recipient))
            },
            value = {
                subhead2_leah(
                    text = "0x7A04536a50d12952f69E071e4c92693939db86b5",
                    textAlign = TextAlign.End
                )
            }
        )
    }
}