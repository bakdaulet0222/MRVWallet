package com.mrv.wallet.modules.market.topcoins

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrv.wallet.R
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatSection
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statMarketTop
import com.mrv.wallet.core.stats.statPeriod
import com.mrv.wallet.core.stats.statSortType
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.market.SortingField
import com.mrv.wallet.modules.market.TopMarket
import com.mrv.wallet.ui.compose.HSSwipeRefresh
import com.mrv.wallet.ui.compose.Select
import com.mrv.wallet.ui.compose.components.AlertGroup
import com.mrv.wallet.ui.compose.components.ButtonSecondaryWithIcon
import com.mrv.wallet.ui.compose.components.CoinListSlidable
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.ListErrorView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopCoins(
    onCoinClick: (String) -> Unit,
) {
    val viewModel = viewModel<MarketTopCoinsViewModel>(
        factory = MarketTopCoinsViewModel.Factory(
            TopMarket.Top100,
            SortingField.TopGainers,
        )
    )

    var openSortingSelector by rememberSaveable { mutableStateOf(false) }
    var openTopSelector by rememberSaveable { mutableStateOf(false) }
    var openPeriodSelector by rememberSaveable { mutableStateOf(false) }

    val uiState = viewModel.uiState

    HSSwipeRefresh(
        refreshing = uiState.isRefreshing,
        topPadding = 44,
        onRefresh = {
            viewModel.refresh()

            stat(page = StatPage.Markets, event = StatEvent.Refresh, section = StatSection.Coins)
        }
    ) {
        Crossfade(uiState.viewState, label = "") { viewState ->
            when (viewState) {
                ViewState.Loading -> {
                    Loading()
                }

                is ViewState.Error -> {
                    ListErrorView(stringResource(R.string.SyncError), viewModel::refresh)
                }

                ViewState.Success -> {
                    val listState = rememberLazyListState()

                    LaunchedEffect(uiState.period, uiState.topMarket, uiState.sortingField) {
                        listState.scrollToItem(0)
                    }

                    CoinListSlidable(
                        listState = listState,
                        items = uiState.viewItems,
                        scrollToTop = false,
                        onAddFavorite = { uid ->
                            viewModel.onAddFavorite(uid)

                            stat(
                                page = StatPage.Markets,
                                event = StatEvent.AddToWatchlist(uid),
                                section = StatSection.Coins
                            )

                        },
                        onRemoveFavorite = { uid ->
                            viewModel.onRemoveFavorite(uid)

                            stat(
                                page = StatPage.Markets,
                                event = StatEvent.RemoveFromWatchlist(uid),
                                section = StatSection.Coins
                            )
                        },
                        onCoinClick = onCoinClick,
                        preItems = {
                            stickyHeader {
                                HeaderSorting(
                                    borderBottom = true,
                                ) {
                                    HSpacer(width = 16.dp)
                                    OptionController(
                                        uiState.sortingField.titleResId,
                                        onOptionClick = {
                                            openSortingSelector = true
                                        }
                                    )
                                    HSpacer(width = 12.dp)
                                    OptionController(
                                        uiState.topMarket.titleResId,
                                        onOptionClick = {
                                            openTopSelector = true
                                        }
                                    )
                                    HSpacer(width = 12.dp)
                                    OptionController(
                                        uiState.period.titleResId,
                                        onOptionClick = {
                                            openPeriodSelector = true
                                        }
                                    )
                                    HSpacer(width = 16.dp)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (openSortingSelector) {
        AlertGroup(
            title = stringResource(R.string.Market_Sort_PopupTitle),
            select = Select(uiState.sortingField, uiState.sortingFields),
            onSelect = { selected ->
                viewModel.onSelectSortingField(selected)
                openSortingSelector = false

                stat(
                    page = StatPage.Markets,
                    event = StatEvent.SwitchSortType(selected.statSortType),
                    section = StatSection.Coins
                )
            },
            onDismiss = {
                openSortingSelector = false
            }
        )
    }
    if (openTopSelector) {
        AlertGroup(
            title = stringResource(R.string.Market_Tab_Coins),
            select = Select(uiState.topMarket, uiState.topMarkets),
            onSelect = {
                viewModel.onSelectTopMarket(it)
                openTopSelector = false

                stat(
                    page = StatPage.Markets,
                    event = StatEvent.SwitchMarketTop(it.statMarketTop),
                    section = StatSection.Coins
                )
            },
            onDismiss = {
                openTopSelector = false
            }
        )
    }
    if (openPeriodSelector) {
        AlertGroup(
            title = stringResource(R.string.CoinPage_Period),
            select = Select(uiState.period, uiState.periods),
            onSelect = { selected ->
                viewModel.onSelectPeriod(selected)
                openPeriodSelector = false

                stat(
                    page = StatPage.Markets,
                    event = StatEvent.SwitchPeriod(selected.statPeriod),
                    section = StatSection.Coins
                )
            },
            onDismiss = {
                openPeriodSelector = false
            }
        )
    }
}

@Composable
fun OptionController(
    label: Int,
    onOptionClick: () -> Unit
) {
    ButtonSecondaryWithIcon(
        modifier = Modifier.height(28.dp),
        onClick = onOptionClick,
        title = stringResource(label),
        iconRight = painterResource(R.drawable.ic_down_arrow_20),
    )
}