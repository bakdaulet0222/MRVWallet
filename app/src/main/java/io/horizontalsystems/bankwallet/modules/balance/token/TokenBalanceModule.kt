package com.mrv.wallet.modules.balance.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.balance.BalanceAdapterRepository
import com.mrv.wallet.modules.balance.BalanceCache
import com.mrv.wallet.modules.balance.BalanceViewItem
import com.mrv.wallet.modules.balance.BalanceViewItemFactory
import com.mrv.wallet.modules.balance.BalanceXRateRepository
import com.mrv.wallet.modules.transactions.NftMetadataService
import com.mrv.wallet.modules.transactions.TransactionRecordRepository
import com.mrv.wallet.modules.transactions.TransactionSyncStateRepository
import com.mrv.wallet.modules.transactions.TransactionViewItem
import com.mrv.wallet.modules.transactions.TransactionViewItemFactory
import com.mrv.wallet.modules.transactions.TransactionsRateRepository

class TokenBalanceModule {

    class Factory(private val wallet: Wallet) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val balanceService = TokenBalanceService(
                wallet,
                BalanceXRateRepository("wallet", App.currencyManager, App.marketKit),
                BalanceAdapterRepository(App.adapterManager, BalanceCache(App.appDatabase.enabledWalletsCacheDao())),
            )

            val tokenTransactionsService = TokenTransactionsService(
                wallet,
                TransactionRecordRepository(App.transactionAdapterManager),
                TransactionsRateRepository(App.currencyManager, App.marketKit),
                TransactionSyncStateRepository(App.transactionAdapterManager),
                App.contactsRepository,
                NftMetadataService(App.nftMetadataManager),
                App.spamManager
            )

            return TokenBalanceViewModel(
                wallet,
                balanceService,
                BalanceViewItemFactory(),
                tokenTransactionsService,
                TransactionViewItemFactory(App.evmLabelManager, App.contactsRepository, App.balanceHiddenManager, App.localStorage),
                App.balanceHiddenManager,
                App.accountManager,
                App.adapterManager,
            ) as T
        }
    }

    data class TokenBalanceUiState(
        val title: String,
        val balanceViewItem: BalanceViewItem?,
        val transactions: Map<String, List<TransactionViewItem>>?,
        val receiveAddressForWatchAccount: String?,
        val failedIconVisible: Boolean,
        val error: TokenBalanceError? = null,
    )

    data class TokenBalanceError(
        val message: String,
        val errorTitle: String? = null,
        val showRetryButton: Boolean = false,
        val showChangeSourceButton: Boolean = false,
    )
}
