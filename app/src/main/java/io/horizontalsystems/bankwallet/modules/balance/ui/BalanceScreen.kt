package com.mrv.wallet.modules.balance.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.modules.balance.BalanceAccountsViewModel
import com.mrv.wallet.modules.balance.BalanceModule
import com.mrv.wallet.modules.balance.BalanceScreenState

@Composable
fun BalanceScreen(navController: NavController) {
    val viewModel = viewModel<BalanceAccountsViewModel>(factory = BalanceModule.AccountsFactory())

    when (val tmpAccount = viewModel.balanceScreenState) {
        BalanceScreenState.NoAccount -> BalanceNoAccount(navController)
        is BalanceScreenState.HasAccount -> {
            BalanceForAccount(navController, tmpAccount.accountViewItem)
        }

        else -> {}
    }
}