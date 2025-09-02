package com.mrv.wallet.modules.tonconnect

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.getInput
import kotlinx.parcelize.Parcelize

class TonConnectMainFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()
        TonConnectMainScreen(navController, input?.deepLinkUri)
    }

    @Parcelize
    data class Input(val deepLinkUri: String) : Parcelable
}
