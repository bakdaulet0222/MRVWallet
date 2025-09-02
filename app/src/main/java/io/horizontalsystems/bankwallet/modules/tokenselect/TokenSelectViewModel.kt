package com.mrv.wallet.modules.tokenselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.ILocalStorage
import com.mrv.wallet.core.ViewModelUiState
import com.mrv.wallet.core.managers.BalanceHiddenManager
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.core.title
import com.mrv.wallet.modules.balance.BalanceModule
import com.mrv.wallet.modules.balance.BalanceService
import com.mrv.wallet.modules.balance.BalanceSortType
import com.mrv.wallet.modules.balance.BalanceSorter
import com.mrv.wallet.modules.balance.BalanceViewItem2
import com.mrv.wallet.modules.balance.BalanceViewItemFactory
import com.mrv.wallet.modules.balance.BalanceViewTypeManager
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TokenSelectViewModel(
    private val service: BalanceService,
    private val balanceViewItemFactory: BalanceViewItemFactory,
    private val balanceViewTypeManager: BalanceViewTypeManager,
    private val itemsFilter: ((BalanceModule.BalanceItem) -> Boolean)?,
    private val balanceSorter: BalanceSorter,
    private val balanceHiddenManager: BalanceHiddenManager,
    private val blockchainTypes: List<BlockchainType>?,
    private val tokenTypes: List<TokenType>?,
    private val localStorage: ILocalStorage,
) : ViewModelUiState<TokenSelectUiState>() {

    private var noItems = false
    private var query: String? = null
    private var balanceViewItems = listOf<BalanceViewItem2>()
    private var availableBlockchainTypes: List<BlockchainType>? = blockchainTypes
    private val allTab = SelectChainTab(title = Translator.getString(R.string.Market_All), null)
    private var selectedChainTab: SelectChainTab = allTab

    override fun createState(): TokenSelectUiState {
        return TokenSelectUiState(
            items = balanceViewItems,
            noItems = noItems,
            selectedTab = selectedChainTab,
            tabs = getTabs()
        )
    }

    override fun onCleared() {
        service.clear()
    }

    init {
        service.start()

        viewModelScope.launch {
            service.balanceItemsFlow.collect { items ->
                val initialFilteredItems = items?.filter { item ->
                    blockchainTypes?.contains(item.wallet.token.blockchainType) ?: true
                }

                availableBlockchainTypes = initialFilteredItems
                    ?.map { it.wallet.token.blockchainType }
                    ?.distinct()

                refreshViewItems(items)
            }
        }
    }

    fun updateFilter(q: String) {
        query = q
        viewModelScope.launch {
            refreshViewItems(service.balanceItemsFlow.value)
        }
    }

    fun onTabSelected(tab: SelectChainTab) {
        selectedChainTab = tab
        viewModelScope.launch {
            refreshViewItems(service.balanceItemsFlow.value)
        }
    }


    private suspend fun refreshViewItems(balanceItems: List<BalanceModule.BalanceItem>?) {
        withContext(Dispatchers.IO) {
            if (balanceItems == null) {
                balanceViewItems = emptyList()
                noItems = true
                emitState()
                return@withContext
            }

            val currentQuery = query // Local copy for thread safety
            val currentSelectedChainTab = selectedChainTab // Local copy

            val filteredItems = balanceItems.asSequence()
                .filter { item ->
                    blockchainTypes?.contains(item.wallet.token.blockchainType) ?: true
                }
                .filter { item ->
                    currentSelectedChainTab.blockchainType?.let { it == item.wallet.token.blockchainType }
                        ?: true
                }
                .filter { item -> tokenTypes?.contains(item.wallet.token.type) ?: true }
                .filter { item -> itemsFilter?.invoke(item) ?: true }
                .filter { item ->
                    if (!currentQuery.isNullOrBlank()) {
                        val coin = item.wallet.coin
                        coin.code.contains(currentQuery, true) || coin.name.contains(
                            currentQuery,
                            true
                        )
                    } else {
                        true
                    }
                }
                .toList()

            noItems = filteredItems.isEmpty()

            val itemsSorted = balanceSorter.sort(filteredItems, BalanceSortType.Value)
            balanceViewItems = itemsSorted.map { balanceItem ->
                balanceViewItemFactory.viewItem2(
                    item = balanceItem,
                    currency = service.baseCurrency,
                    hideBalance = balanceHiddenManager.balanceHidden,
                    watchAccount = service.isWatchAccount,
                    balanceViewType = balanceViewTypeManager.balanceViewTypeFlow.value,
                    networkAvailable = service.networkAvailable,
                    amountRoundingEnabled = localStorage.amountRoundingEnabled
                )
            }

            emitState()
        }
    }

    private fun getTabs(): List<SelectChainTab> {
        val currentAvailableBlockchainTypes = availableBlockchainTypes
        if (currentAvailableBlockchainTypes.isNullOrEmpty() || currentAvailableBlockchainTypes.size == 1) {
            return emptyList()
        }

        return listOf(allTab) + currentAvailableBlockchainTypes.map { blockchainType ->
            SelectChainTab(
                title = blockchainType.title,
                blockchainType = blockchainType
            )
        }
    }

    class FactoryForSend(
        private val blockchainTypes: List<BlockchainType>? = null,
        private val tokenTypes: List<TokenType>? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TokenSelectViewModel(
                service = BalanceService.getInstance("wallet"),
                balanceViewItemFactory = BalanceViewItemFactory(),
                balanceViewTypeManager = App.balanceViewTypeManager,
                itemsFilter = null,
                balanceSorter = BalanceSorter(),
                balanceHiddenManager = App.balanceHiddenManager,
                blockchainTypes = blockchainTypes,
                tokenTypes = tokenTypes,
                localStorage = App.localStorage
            ) as T
        }
    }
}

data class TokenSelectUiState(
    val items: List<BalanceViewItem2>,
    val noItems: Boolean,
    val selectedTab: SelectChainTab,
    val tabs: List<SelectChainTab>,
)

data class SelectChainTab(
    val title: String,
    val blockchainType: BlockchainType?,
)
