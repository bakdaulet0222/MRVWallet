package com.mrv.wallet.modules.settings.addresschecker

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.modules.settings.addresschecker.ui.UnifiedAddressCheckScreen

class AddressCheckFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        UnifiedAddressCheckScreen(
            onClose = { navController.popBackStack() }
        )
    }
}
