package com.mrv.wallet.modules.receive

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.modules.receive.ui.UsedAddressScreen
import com.mrv.wallet.modules.receive.ui.UsedAddressesParams

class BtcUsedAddressesFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        withInput<UsedAddressesParams>(navController) {
            UsedAddressScreen(it) { navController.popBackStack() }
        }
    }
}
