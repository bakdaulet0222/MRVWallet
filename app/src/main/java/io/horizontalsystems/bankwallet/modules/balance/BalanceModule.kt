package com.mrv.wallet.modules.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.R
import com.mrv.wallet.core.AdapterState
import com.mrv.wallet.core.App
import com.mrv.wallet.core.BalanceData
import com.mrv.wallet.core.Warning
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.address.AddressHandlerFactory
import com.mrv.wallet.ui.compose.TranslatableString
import io.horizontalsystems.marketkit.models.CoinPrice

object BalanceModule {
    class AccountsFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BalanceAccountsViewModel(App.accountManager) as T
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val totalService = TotalService(
                App.currencyManager,
                App.marketKit,
                App.baseTokenManager,
                App.balanceHiddenManager,
                App.localStorage,
            )
            return BalanceViewModel(
                BalanceService.getInstance("wallet"),
                BalanceViewItemFactory(),
                App.balanceViewTypeManager,
                TotalBalance(totalService, App.balanceHiddenManager),
                App.localStorage,
                App.wcManager,
                AddressHandlerFactory(App.appConfigProvider.udnApiKey),
                App.priceManager,
                App.instance.isSwapEnabled
            ) as T
        }
    }

    data class BalanceItem(
        val wallet: Wallet,
        val balanceData: BalanceData,
        val state: AdapterState,
        val sendAllowed: Boolean,
        val coinPrice: CoinPrice?,
        val warning: BalanceWarning? = null
    ) {
        val balanceFiatTotal by lazy {
            coinPrice?.value?.let { balanceData.total.times(it) }
        }
    }

    sealed class BalanceWarning : Warning() {
        data object TronInactiveAccountWarning : BalanceWarning()
    }

    val BalanceWarning.warningText: WarningText
        get() = when (this) {
            BalanceWarning.TronInactiveAccountWarning -> WarningText(
                title = TranslatableString.ResString(R.string.Tron_TokenPage_AddressNotActive_Title),
                text = TranslatableString.ResString(R.string.Tron_TokenPage_AddressNotActive_Info),
            )
        }
}