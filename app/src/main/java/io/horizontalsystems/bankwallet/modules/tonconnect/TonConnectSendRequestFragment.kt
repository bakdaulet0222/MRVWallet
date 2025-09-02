package com.mrv.wallet.modules.tonconnect

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mrv.wallet.core.BaseComposeFragment

class TonConnectSendRequestFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        TonConnectSendRequestScreen(navController)
    }
}
