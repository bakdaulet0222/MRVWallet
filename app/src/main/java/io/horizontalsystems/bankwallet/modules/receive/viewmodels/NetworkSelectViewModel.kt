package com.mrv.wallet.modules.receive.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.core.IWalletManager
import com.mrv.wallet.core.eligibleTokens
import com.mrv.wallet.core.utils.Utils
import com.mrv.wallet.entities.Account
import com.mrv.wallet.entities.Wallet
import io.horizontalsystems.marketkit.models.FullCoin
import io.horizontalsystems.marketkit.models.Token

class NetworkSelectViewModel(
    val activeAccount: Account,
    val fullCoin: FullCoin,
    private val walletManager: IWalletManager
) : ViewModel() {
    val eligibleTokens = fullCoin.eligibleTokens(activeAccount.type)

    suspend fun getOrCreateWallet(token: Token): Wallet {
        return walletManager
            .activeWallets
            .find { it.token == token }
            ?: createWallet(token)
    }

    private suspend fun createWallet(token: Token): Wallet {
        val wallet = Wallet(token, activeAccount)

        walletManager.save(listOf(wallet))

        Utils.waitUntil(1000L, 100L) {
            App.adapterManager.getReceiveAdapterForWallet(wallet) != null
        }

        return wallet
    }

    class Factory(
        private val activeAccount: Account,
        private val fullCoin: FullCoin
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NetworkSelectViewModel(activeAccount, fullCoin, App.walletManager) as T
        }
    }
}
