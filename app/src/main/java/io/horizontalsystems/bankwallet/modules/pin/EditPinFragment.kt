package com.mrv.wallet.modules.pin

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.modules.pin.ui.PinSet

class EditPinFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        PinSet(
            title = stringResource(R.string.EditPin_Title),
            description = stringResource(R.string.EditPin_NewPinInfo),
            dismissWithSuccess = { navController.popBackStack() },
            onBackPress = { navController.popBackStack() }
        )
    }
}
