package com.mrv.wallet.modules.transactionInfo.options

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment

class TransactionSpeedUpCancelTransactionSettingsFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        TransactionSpeedUpCancelTransactionSettingsScreen(navController)
    }
}

@Composable
fun TransactionSpeedUpCancelTransactionSettingsScreen(navController: NavController) {
    val viewModelStoreOwner = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(R.id.transactionSpeedUpCancelFragment)
    }

    val viewModel = viewModel<TransactionSpeedUpCancelViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
    )

    val sendTransactionService = viewModel.sendTransactionService

    sendTransactionService.GetSettingsContent(navController)
}
