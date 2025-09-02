package com.mrv.wallet.modules.multiswap.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.modules.multiswap.QuoteInfoRow
import com.mrv.wallet.ui.compose.components.subhead2_grey
import com.mrv.wallet.ui.compose.components.subhead2_leah

data class DataFieldNonce(val nonce: Long) : DataField {
    @Composable
    override fun GetContent(navController: NavController, borderTop: Boolean) {
        QuoteInfoRow(
            borderTop = borderTop,
            title = {
                subhead2_grey(text = stringResource(R.string.Send_Confirmation_Nonce))
            },
            value = {
                subhead2_leah(text = nonce.toString())
            }
        )
    }
}
