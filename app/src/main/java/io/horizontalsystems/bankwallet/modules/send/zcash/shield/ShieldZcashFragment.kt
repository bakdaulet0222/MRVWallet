package com.mrv.wallet.modules.send.zcash.shield

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.getInput
import com.mrv.wallet.entities.Wallet
import kotlinx.parcelize.Parcelize

class ShieldZcashFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()

        input?.let {
            val viewModel = viewModel<ShieldZcashViewModel>(factory = ShieldZcashModule.Factory(input.wallet))
            ShieldZcashScreen(navController, viewModel, input.entryPointDestId)
        }
    }

    @Parcelize
    data class Input(
        val wallet: Wallet,
        val entryPointDestId: Int
    ) : Parcelable

}
