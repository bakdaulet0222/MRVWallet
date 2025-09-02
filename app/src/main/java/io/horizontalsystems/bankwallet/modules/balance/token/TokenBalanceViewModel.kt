package com.mrv.wallet.modules.balance.token

import androidx.lifecycle.viewModelScope
import com.mrv.wallet.R
import com.mrv.wallet.core.IAccountManager
import com.mrv.wallet.core.IAdapterManager
import com.mrv.wallet.core.ViewModelUiState
import com.mrv.wallet.core.badge
import com.mrv.wallet.core.managers.BalanceHiddenManager
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.balance.BackupRequiredError
import com.mrv.wallet.modules.balance.BalanceModule
import com.mrv.wallet.modules.balance.BalanceViewItem
import com.mrv.wallet.modules.balance.BalanceViewItemFactory
import com.mrv.wallet.modules.balance.BalanceViewType
import com.mrv.wallet.modules.balance.token.TokenBalanceModule.TokenBalanceUiState
import com.mrv.wallet.modules.transactions.TransactionItem
import com.mrv.wallet.modules.transactions.TransactionViewItem
import com.mrv.wallet.modules.transactions.TransactionViewItemFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class TokenBalanceViewModel(
    private val wallet: Wallet,
    private val balanceService: TokenBalanceService,
    private val balanceViewItemFactory: BalanceViewItemFactory,
    private val transactionsService: TokenTransactionsService,
    private val transactionViewItem2Factory: TransactionViewItemFactory,
    private val balanceHiddenManager: BalanceHiddenManager,
    private val accountManager: IAccountManager,
    private val adapterManager: IAdapterManager,
) : ViewModelUiState<TokenBalanceUiState>() {

    private val title = wallet.token.coin.code + wallet.token.badge?.let { " ($it)" }.orEmpty()

    private var balanceViewItem: BalanceViewItem? = null
    private var transactions: Map<String, List<TransactionViewItem>>? = null
    private var addressForWatchAccount: String? = null
    private var error: TokenBalanceModule.TokenBalanceError? = null
    private var failedIconVisible = false
    private var loadingTransactions = true

    init {
        viewModelScope.launch(Dispatchers.IO) {
            balanceService.balanceItemFlow.collect { balanceItem ->
                balanceItem?.let {
                    updateBalanceViewItem(it)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            balanceHiddenManager.balanceHiddenFlow.collect {
                balanceService.balanceItem?.let {
                    updateBalanceViewItem(it)
                    transactionViewItem2Factory.updateCache()
                    transactionsService.refreshList()
                }
            }
        }

        viewModelScope.launch {
            transactionsService.itemsObservable.asFlow().collect {
                updateTransactions(it)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            balanceService.start()
            delay(300)
            transactionsService.start()
        }
    }

    override fun createState() = TokenBalanceUiState(
        title = title,
        balanceViewItem = balanceViewItem,
        transactions = transactions,
        receiveAddressForWatchAccount = addressForWatchAccount,
        failedIconVisible = failedIconVisible,
        error = error
    )

    private fun setReceiveAddressForWatchAccount() {
        addressForWatchAccount = adapterManager.getReceiveAdapterForWallet(wallet)?.receiveAddress
        emitState()
    }

    private fun updateTransactions(items: List<TransactionItem>) {
        transactions = items
            .map { transactionViewItem2Factory.convertToViewItemCached(it) }
            .groupBy { it.formattedDate }

        loadingTransactions = false
        updateErrorState()
        emitState()
    }

    private fun updateBalanceViewItem(balanceItem: BalanceModule.BalanceItem) {

        val balanceViewItem = balanceViewItemFactory.viewItem(
            balanceItem,
            balanceService.baseCurrency,
            balanceHiddenManager.balanceHidden,
            wallet.account.isWatchAccount,
            BalanceViewType.CoinThenFiat
        )

        failedIconVisible = balanceViewItem.failedIconVisible

        if (wallet.account.isWatchAccount) {
            setReceiveAddressForWatchAccount()
        }

        this.balanceViewItem = balanceViewItem.copy(
            primaryValue = balanceViewItem.primaryValue.copy(value = balanceViewItem.primaryValue.value + " " + balanceViewItem.wallet.coin.code)
        )

        updateErrorState()
        emitState()
    }

    private fun updateErrorState() {
        if (!loadingTransactions && transactions.isNullOrEmpty()) {
            error = if (balanceViewItem?.syncingProgress?.progress != null) {
                TokenBalanceModule.TokenBalanceError(
                    message = Translator.getString(R.string.Transactions_WaitForSync),
                )
            } else if (balanceViewItem?.warning != null) {
                balanceViewItem?.warning?.let{
                    TokenBalanceModule.TokenBalanceError(
                        message = it.text.toString(),
                        errorTitle = it.title.toString()
                    )
                }
            } else {
                TokenBalanceModule.TokenBalanceError(
                    message = Translator.getString(R.string.Transactions_EmptyList)
                )
            }
        } else {
            error = null
        }
    }

    @Throws(BackupRequiredError::class, IllegalStateException::class)
    fun getWalletForReceive(): Wallet {
        val account =
            accountManager.activeAccount ?: throw IllegalStateException("Active account is not set")
        when {
            account.hasAnyBackup -> return wallet
            else -> throw BackupRequiredError(account, wallet.coin.name)
        }
    }

    fun onBottomReached() {
        transactionsService.loadNext()
    }

    fun willShow(viewItem: TransactionViewItem) {
        transactionsService.fetchRateIfNeeded(viewItem.uid)
    }

    fun getTransactionItem(viewItem: TransactionViewItem) =
        transactionsService.getTransactionItem(viewItem.uid)

    fun toggleBalanceVisibility() {
        balanceHiddenManager.toggleBalanceHidden()
    }

    override fun onCleared() {
        super.onCleared()

        balanceService.clear()
    }

}
