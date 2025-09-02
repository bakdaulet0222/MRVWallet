package com.mrv.wallet.modules.watchaddress.selectblockchains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.entities.AccountType
import com.mrv.wallet.modules.watchaddress.WatchAddressService

object SelectBlockchainsModule {
    class Factory(val accountType: AccountType, val accountName: String?) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val service = WatchAddressService(App.accountManager, App.walletActivator, App.accountFactory, App.marketKit, App.evmBlockchainManager)
            return SelectBlockchainsViewModel(accountType, accountName, service) as T
        }
    }
}
