package com.mrv.wallet.modules.balance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.walletconnect.web3.wallet.client.Wallet.Params.Pair
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.mrv.wallet.R
import com.mrv.wallet.core.AdapterState
import com.mrv.wallet.core.App
import com.mrv.wallet.core.ILocalStorage
import com.mrv.wallet.core.ViewModelUiState
import com.mrv.wallet.core.managers.PriceManager
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.utils.AddressUriParser
import com.mrv.wallet.core.utils.ToncoinUriParser
import com.mrv.wallet.entities.Account
import com.mrv.wallet.entities.AddressUri
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.address.AddressHandlerFactory
import com.mrv.wallet.modules.walletconnect.WCManager
import com.mrv.wallet.modules.walletconnect.list.WalletConnectListModule
import com.mrv.wallet.modules.walletconnect.list.WalletConnectListViewModel
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.math.BigDecimal

class BalanceViewModel(
    private val service: BalanceService,
    private val balanceViewItemFactory: BalanceViewItemFactory,
    private val balanceViewTypeManager: BalanceViewTypeManager,
    private val totalBalance: TotalBalance,
    private val localStorage: ILocalStorage,
    private val wCManager: WCManager,
    private val addressHandlerFactory: AddressHandlerFactory,
    private val priceManager: PriceManager,
    val isSwapEnabled: Boolean
) : ViewModelUiState<BalanceUiState>(), ITotalBalance by totalBalance {

    private var balanceViewType = balanceViewTypeManager.balanceViewTypeFlow.value
    private var viewState: ViewState? = null
    private var balanceViewItems = listOf<BalanceViewItem2>()
    private var isRefreshing = false
    private var openSendTokenSelect: OpenSendTokenSelect? = null
    private var errorMessage: String? = null
    private var balanceTabButtonsEnabled = localStorage.balanceTabButtonsEnabled

    private val sortTypes =
        listOf(BalanceSortType.Value, BalanceSortType.Name, BalanceSortType.PercentGrowth)
    private var sortType = service.sortType

    var connectionResult by mutableStateOf<WalletConnectListViewModel.ConnectionResult?>(null)
        private set

    private var refreshViewItemsJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.Default) {
            service.balanceItemsFlow
                .collect { items ->
                    totalBalance.setTotalServiceItems(items?.map {
                        TotalService.BalanceItem(
                            it.balanceData.total,
                            it.state !is AdapterState.Synced,
                            it.coinPrice
                        )
                    })

                    refreshViewItems(items)
                }
        }

        viewModelScope.launch {
            totalBalance.stateFlow.collect {
                refreshViewItems(service.balanceItemsFlow.value)
            }
        }

        viewModelScope.launch {
            balanceViewTypeManager.balanceViewTypeFlow.collect {
                handleUpdatedBalanceViewType(it)
            }
        }

        viewModelScope.launch {
            localStorage.balanceTabButtonsEnabledFlow.collect {
                balanceTabButtonsEnabled = it
                emitState()
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            priceManager.priceChangeIntervalFlow.collect {
                refreshViewItems(service.balanceItemsFlow.value)
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            localStorage.amountRoundingEnabledFlow.collect{
                refreshViewItems(service.balanceItemsFlow.value)
            }
        }

        service.start()

        totalBalance.start(viewModelScope)
    }

    override fun createState() = BalanceUiState(
        balanceViewItems = balanceViewItems,
        viewState = viewState,
        isRefreshing = isRefreshing,
        headerNote = headerNote(),
        errorMessage = errorMessage,
        openSend = openSendTokenSelect,
        balanceTabButtonsEnabled = balanceTabButtonsEnabled,
        sortType = sortType,
        sortTypes = sortTypes,
    )

    private suspend fun handleUpdatedBalanceViewType(balanceViewType: BalanceViewType) {
        this.balanceViewType = balanceViewType

        service.balanceItemsFlow.value?.let {
            refreshViewItems(it)
        }
    }

    private fun headerNote(): HeaderNote {
        val account = service.account ?: return HeaderNote.None
        val nonRecommendedDismissed =
            localStorage.nonRecommendedAccountAlertDismissedAccounts.contains(account.id)

        return account.headerNote(nonRecommendedDismissed)
    }

    private fun refreshViewItems(balanceItems: List<BalanceModule.BalanceItem>?) {
        refreshViewItemsJob?.cancel()
        refreshViewItemsJob = viewModelScope.launch(Dispatchers.Default) {
            if (balanceItems != null) {
                viewState = ViewState.Success
                balanceViewItems = balanceItems.map { balanceItem ->
                    ensureActive()
                    balanceViewItemFactory.viewItem2(
                        balanceItem,
                        service.baseCurrency,
                        balanceHidden,
                        service.isWatchAccount,
                        balanceViewType,
                        service.networkAvailable,
                        localStorage.amountRoundingEnabled
                    )
                }
            } else {
                viewState = null
                balanceViewItems = listOf()
            }

            ensureActive()
            emitState()
        }
    }

    fun onHandleRoute() {
        connectionResult = null
    }

    override fun onCleared() {
        totalBalance.stop()
        service.clear()
    }

    fun onRefresh() {
        if (isRefreshing) {
            return
        }

        stat(page = StatPage.Balance, event = StatEvent.Refresh)

        viewModelScope.launch(Dispatchers.Default) {
            isRefreshing = true
            emitState()

            service.refresh()
            // A fake 2 seconds 'refresh'
            delay(2300)

            isRefreshing = false
            emitState()
        }
    }

    fun setSortType(sortType: BalanceSortType) {
        this.sortType = sortType
        emitState()

        viewModelScope.launch(Dispatchers.Default) {
            service.sortType = sortType
        }
    }

    fun onCloseHeaderNote(headerNote: HeaderNote) {
        when (headerNote) {
            HeaderNote.NonRecommendedAccount -> {
                service.account?.let { account ->
                    localStorage.nonRecommendedAccountAlertDismissedAccounts += account.id
                    emitState()
                }
            }

            else -> Unit
        }
    }

    fun disable(viewItem: BalanceViewItem2) {
        service.disable(viewItem.wallet)

        stat(page = StatPage.Balance, event = StatEvent.DisableToken(viewItem.wallet.token))
    }

    fun getSyncErrorDetails(viewItem: BalanceViewItem2): SyncError = when {
        service.networkAvailable -> SyncError.Dialog(viewItem.wallet, viewItem.errorMessage)
        else -> SyncError.NetworkNotAvailable()
    }

    fun getReceiveAllowedState(): ReceiveAllowedState? {
        val tmpAccount = service.account ?: return null
        return when {
            tmpAccount.hasAnyBackup -> ReceiveAllowedState.Allowed
            else -> ReceiveAllowedState.BackupRequired(tmpAccount)
        }
    }

    fun getWalletConnectSupportState(): WCManager.SupportState {
        return wCManager.getWalletConnectSupportState()
    }

    fun handleScannedData(scannedText: String) {
        viewModelScope.launch {
            if (
                scannedText.startsWith("tc:") ||
                scannedText.startsWith("https://unstoppable.money/ton-connect")
            ) {
                App.tonConnectManager.handle(scannedText)
            } else {
                val wcUriVersion = WalletConnectListModule.getVersionFromUri(scannedText)
                if (wcUriVersion == 2) {
                    handleWalletConnectUri(scannedText)
                } else {
                    handleAddressData(scannedText)
                }
            }
        }
    }

    private fun handleAddressData(text: String) {
        if (text.contains("//")) {
            //handle this type of uri ton://transfer/<address>
            val toncoinAddress = ToncoinUriParser.getAddress(text) ?: return
            openSendTokenSelect = OpenSendTokenSelect(
                blockchainTypes = listOf(BlockchainType.Ton),
                tokenTypes = null,
                address = toncoinAddress,
                amount = null
            )
            emitState()
            return
        }

        val uri = AddressUriParser.addressUri(text)
        if (uri != null) {
            val allowedBlockchainTypes = uri.allowedBlockchainTypes
            var allowedTokenTypes: List<TokenType>? = null
            uri.value<String>(AddressUri.Field.TokenUid)?.let { uid ->
                TokenType.fromId(uid)?.let { tokenType ->
                    allowedTokenTypes = listOf(tokenType)
                }
            }

            openSendTokenSelect = OpenSendTokenSelect(
                blockchainTypes = allowedBlockchainTypes,
                tokenTypes = allowedTokenTypes,
                address = uri.address,
                amount = uri.amount
            )
            emitState()
        } else {
            val chain = addressHandlerFactory.parserChain(null)
            val types = chain.supportedAddressHandlers(text)
            if (types.isEmpty()) {
                errorMessage = Translator.getString(R.string.Balance_Error_InvalidQrCode)
                emitState()
                return
            }

            openSendTokenSelect = OpenSendTokenSelect(
                blockchainTypes = types.map { it.blockchainType },
                tokenTypes = null,
                address = text,
                amount = null
            )
            emitState()
        }
    }

    private fun handleWalletConnectUri(scannedText: String) {
        Web3Wallet.pair(Pair(scannedText.trim()),
            onSuccess = {
                connectionResult = null
            },
            onError = {
                connectionResult = WalletConnectListViewModel.ConnectionResult.Error
            }
        )
    }

    fun onSendOpened() {
        openSendTokenSelect = null
        emitState()
    }

    fun errorShown() {
        errorMessage = null
        emitState()
    }

    sealed class SyncError {
        class NetworkNotAvailable : SyncError()
        class Dialog(val wallet: Wallet, val errorMessage: String?) : SyncError()
    }
}

sealed class ReceiveAllowedState {
    object Allowed : ReceiveAllowedState()
    data class BackupRequired(val account: Account) : ReceiveAllowedState()
}

class BackupRequiredError(val account: Account, val coinTitle: String) : Error("Backup Required")

data class BalanceUiState(
    val balanceViewItems: List<BalanceViewItem2>,
    val viewState: ViewState?,
    val isRefreshing: Boolean,
    val headerNote: HeaderNote,
    val errorMessage: String?,
    val openSend: OpenSendTokenSelect? = null,
    val balanceTabButtonsEnabled: Boolean,
    val sortType: BalanceSortType,
    val sortTypes: List<BalanceSortType>,
)

data class OpenSendTokenSelect(
    val blockchainTypes: List<BlockchainType>?,
    val tokenTypes: List<TokenType>?,
    val address: String,
    val amount: BigDecimal? = null,
)

sealed class TotalUIState {
    data class Visible(
        val primaryAmountStr: String,
        val secondaryAmountStr: String,
        val dimmed: Boolean
    ) : TotalUIState()

    object Hidden : TotalUIState()

}

enum class HeaderNote {
    None,
    NonStandardAccount,
    NonRecommendedAccount
}

fun Account.headerNote(nonRecommendedDismissed: Boolean): HeaderNote = when {
    nonStandard -> HeaderNote.NonStandardAccount
    nonRecommended -> if (nonRecommendedDismissed) HeaderNote.None else HeaderNote.NonRecommendedAccount
    else -> HeaderNote.None
}