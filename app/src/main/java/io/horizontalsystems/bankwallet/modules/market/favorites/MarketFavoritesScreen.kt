package com.mrv.wallet.modules.market.favorites

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.paidAction
import com.mrv.wallet.core.slideFromBottomForResult
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatPremiumTrigger
import com.mrv.wallet.core.stats.StatSection
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statPeriod
import com.mrv.wallet.core.stats.statSortType
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.CoinFragment
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.market.filtersresult.SignalButton
import com.mrv.wallet.modules.market.topcoins.OptionController
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.HSSwipeRefresh
import com.mrv.wallet.ui.compose.Select
import com.mrv.wallet.ui.compose.components.AlertGroup
import com.mrv.wallet.ui.compose.components.ButtonSecondaryCircle
import com.mrv.wallet.ui.compose.components.CoinListOrderable
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.ListEmptyView
import com.mrv.wallet.ui.compose.components.ListErrorView
import io.horizontalsystems.subscriptions.core.TradeSignals

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarketFavoritesScreen(
    navController: NavController
) {
    val viewModel = viewModel<MarketFavoritesViewModel>(factory = MarketFavoritesModule.Factory())
    val uiState = viewModel.uiState
    var openSortingSelector by rememberSaveable { mutableStateOf(false) }
    var openPeriodSelector by rememberSaveable { mutableStateOf(false) }
    var scrollToTopAfterUpdate by rememberSaveable { mutableStateOf(false) }
    var manualOrderEnabled by rememberSaveable { mutableStateOf(false) }

    HSSwipeRefresh(
        refreshing = uiState.isRefreshing,
        topPadding = 44,
        onRefresh = {
            viewModel.refresh()

            stat(
                page = StatPage.Markets,
                event = StatEvent.Refresh,
                section = StatSection.Watchlist
            )
        }
    ) {
        Crossfade(
            targetState = uiState.viewState,
            label = ""
        ) { viewState ->
            when (viewState) {
                ViewState.Loading -> {
                    Loading()
                }

                is ViewState.Error -> {
                    ListErrorView(stringResource(R.string.SyncError), viewModel::onErrorClick)
                }

                ViewState.Success -> {
                    if (uiState.viewItems.isEmpty()) {
                        ListEmptyView(
                            text = stringResource(R.string.Market_Tab_Watchlist_EmptyList),
                            icon = R.drawable.ic_heart_24
                        )
                    } else {
                        CoinListOrderable(
                            items = uiState.viewItems,
                            scrollToTop = scrollToTopAfterUpdate,
                            onAddFavorite = { /*not used */ },
                            onRemoveFavorite = { uid ->
                                viewModel.removeFromFavorites(uid)

                                stat(
                                    page = StatPage.Markets,
                                    event = StatEvent.RemoveFromWatchlist(uid),
                                    section = StatSection.Watchlist
                                )
                            },
                            onCoinClick = { coinUid ->
                                val arguments = CoinFragment.Input(coinUid)
                                navController.slideFromRight(R.id.coinFragment, arguments)

                                stat(
                                    page = StatPage.Markets,
                                    event = StatEvent.OpenCoin(coinUid),
                                    section = StatSection.Watchlist
                                )
                            },
                            onReorder = { from, to ->
                                viewModel.reorder(from, to)
                            },
                            canReorder = uiState.sortingField == WatchlistSorting.Manual,
                            showReorderArrows = uiState.sortingField == WatchlistSorting.Manual && manualOrderEnabled,
                            enableManualOrder = {
                                manualOrderEnabled = true
                            },
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
                                        if (uiState.sortingField == WatchlistSorting.Manual) {
                                            HSpacer(width = 12.dp)
                                            ButtonSecondaryCircle(
                                                icon = R.drawable.ic_edit_20,
                                                tint = if (manualOrderEnabled) ComposeAppTheme.colors.dark else ComposeAppTheme.colors.leah,
                                                background = if (manualOrderEnabled) ComposeAppTheme.colors.jacob else ComposeAppTheme.colors.blade,
                                            ) {
                                                manualOrderEnabled = !manualOrderEnabled
                                            }
                                        }
                                        HSpacer(width = 12.dp)
                                        OptionController(
                                            uiState.period.titleResId,
                                            onOptionClick = {
                                                openPeriodSelector = true
                                            }
                                        )
                                        HSpacer(width = 12.dp)
                                        SignalButton(
                                            turnedOn = uiState.showSignal,
                                            onToggle = {
                                                if (it) {
                                                    navController.paidAction(TradeSignals) {
                                                        navController.slideFromBottomForResult<MarketSignalsFragment.Result>(
                                                            R.id.marketSignalsFragment
                                                        ) {
                                                            if (it.enabled) {
                                                                viewModel.showSignals()
                                                            }
                                                        }
                                                    }
                                                    stat(
                                                        page = StatPage.MarketOverview,
                                                        event = StatEvent.OpenPremium(
                                                            StatPremiumTrigger.TradingSignal),
                                                        section = StatSection.Watchlist
                                                    )
                                                } else {
                                                    viewModel.hideSignals()
                                                }
                                            })
                                        HSpacer(width = 16.dp)
                                    }
                                }
                            }
                        )
                        if (scrollToTopAfterUpdate) {
                            scrollToTopAfterUpdate = false
                        }
                    }
                }
            }
        }
    }

    if (openSortingSelector) {
        AlertGroup(
            title = stringResource(R.string.Market_Sort_PopupTitle),
            select = Select(uiState.sortingField, viewModel.sortingOptions),
            onSelect = { selected ->
                manualOrderEnabled = false
                openSortingSelector = false
                scrollToTopAfterUpdate = true
                viewModel.onSelectSortingField(selected)

                stat(
                    page = StatPage.Markets,
                    event = StatEvent.SwitchSortType(selected.statSortType),
                    section = StatSection.Watchlist
                )
            },
            onDismiss = {
                openSortingSelector = false
            }
        )
    }
    if (openPeriodSelector) {
        AlertGroup(
            title = stringResource(R.string.CoinPage_Period),
            select = Select(uiState.period, viewModel.periods),
            onSelect = { selected ->
                openPeriodSelector = false
                scrollToTopAfterUpdate = true
                viewModel.onSelectPeriod(selected)

                stat(
                    page = StatPage.Markets,
                    event = StatEvent.SwitchPeriod(selected.statPeriod),
                    section = StatSection.Watchlist
                )
            },
            onDismiss = {
                openPeriodSelector = false
            }
        )
    }

}
