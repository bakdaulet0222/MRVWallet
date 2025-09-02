package com.mrv.wallet.modules.balance.token

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.transactions.TransactionsModule
import com.mrv.wallet.modules.transactions.TransactionsViewModel

class TokenBalanceFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Wallet>(navController) { wallet ->
            val viewModel by viewModels<TokenBalanceViewModel> { TokenBalanceModule.Factory(wallet) }
            val transactionsViewModel by navGraphViewModels<TransactionsViewModel>(R.id.mainFragment) { TransactionsModule.Factory() }

            TokenBalanceScreen(
                viewModel,
                transactionsViewModel,
                navController
            )
        }
    }
}
