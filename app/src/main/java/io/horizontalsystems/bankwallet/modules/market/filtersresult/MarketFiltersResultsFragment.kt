package com.mrv.wallet.modules.market.filtersresult

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.paidAction
import com.mrv.wallet.core.slideFromBottomForResult
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatPremiumTrigger
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.CoinFragment
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.market.favorites.MarketSignalsFragment
import com.mrv.wallet.modules.market.filters.MarketFiltersViewModel
import com.mrv.wallet.modules.market.topcoins.OptionController
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AlertGroup
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.ButtonPrimaryDefaults
import com.mrv.wallet.ui.compose.components.ButtonSecondary
import com.mrv.wallet.ui.compose.components.CoinList
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.ListErrorView
import com.mrv.wallet.ui.compose.components.SecondaryButtonDefaults
import io.horizontalsystems.subscriptions.core.TradeSignals

class MarketFiltersResultsFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val viewModel = getViewModel()

        if (viewModel == null) {
            navController.popBackStack()
            return
        }

        SearchResultsScreen(viewModel, navController)
    }

    private fun getViewModel(): MarketFiltersResultViewModel? {
        return try {
            val marketSearchFilterViewModel by navGraphViewModels<MarketFiltersViewModel>(R.id.marketAdvancedSearchFragment)
            val viewModel by viewModels<MarketFiltersResultViewModel> {
                MarketFiltersResultsModule.Factory(marketSearchFilterViewModel.service)
            }
            viewModel
        } catch (e: RuntimeException) {
            null
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsScreen(
    viewModel: MarketFiltersResultViewModel,
    navController: NavController
) {

    val uiState = viewModel.uiState
    var scrollToTopAfterUpdate by rememberSaveable { mutableStateOf(false) }
    var openSortingSelector by rememberSaveable { mutableStateOf(false) }

    Surface(
        color = ComposeAppTheme.colors.tyler,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column {
            AppBar(
                title = stringResource(R.string.Market_AdvancedSearch_Results),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
            )

            Crossfade(uiState.viewState, label = "") { state ->
                when (state) {
                    ViewState.Loading -> {
                        Loading()
                    }

                    is ViewState.Error -> {
                        ListErrorView(stringResource(R.string.SyncError), viewModel::onErrorClick)
                    }

                    ViewState.Success -> {
                        CoinList(
                            items = uiState.viewItems,
                            scrollToTop = scrollToTopAfterUpdate,
                            onAddFavorite = { uid ->
                                viewModel.onAddFavorite(uid)

                                stat(
                                    page = StatPage.AdvancedSearchResults,
                                    event = StatEvent.AddToWatchlist(uid)
                                )
                            },
                            onRemoveFavorite = { uid ->
                                viewModel.onRemoveFavorite(uid)

                                stat(
                                    page = StatPage.AdvancedSearchResults,
                                    event = StatEvent.RemoveFromWatchlist(uid)
                                )
                            },
                            onCoinClick = { coinUid ->
                                val arguments = CoinFragment.Input(coinUid)
                                navController.slideFromRight(R.id.coinFragment, arguments)

                                stat(
                                    page = StatPage.AdvancedSearchResults,
                                    event = StatEvent.OpenCoin(coinUid)
                                )
                            },
                            preItems = {
                                stickyHeader {
                                    HeaderSorting(borderBottom = true, borderTop = true) {
                                        HSpacer(width = 16.dp)
                                        OptionController(
                                            uiState.sortingField.titleResId,
                                            onOptionClick = {
                                                openSortingSelector = true
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
                                                        page = StatPage.AdvancedSearchResults,
                                                        event = StatEvent.OpenPremium(
                                                            StatPremiumTrigger.TradingSignal)
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

            if (openSortingSelector) {
                AlertGroup(
                    title = stringResource(R.string.Market_Sort_PopupTitle),
                    select = uiState.selectSortingField,
                    onSelect = { selected ->
                        viewModel.onSelectSortingField(selected)
                        openSortingSelector = false
                        scrollToTopAfterUpdate = true
                    },
                    onDismiss = {
                        openSortingSelector = false
                    }
                )
            }

        }
    }
}

@Composable
fun SignalButton(turnedOn: Boolean, onToggle: (Boolean) -> Unit) {
    val title = stringResource(id = R.string.Market_Signals)
    val onClick = { onToggle.invoke(!turnedOn) }
    val buttonColors = if (turnedOn) {
        ButtonPrimaryDefaults.textButtonColors(
            backgroundColor = ComposeAppTheme.colors.yellowD,
            contentColor = ComposeAppTheme.colors.dark,
            disabledBackgroundColor = ComposeAppTheme.colors.blade,
            disabledContentColor = ComposeAppTheme.colors.andy,
        )
    } else {
        SecondaryButtonDefaults.buttonColors()
    }
    ButtonSecondary(
        onClick = onClick,
        contentPadding = PaddingValues(
            start = 10.dp,
            end = 16.dp,
        ),
        buttonColors = buttonColors,
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(end = 2.dp),
                    painter = painterResource(R.drawable.ic_star_filled_20),
                    contentDescription = null,
                    tint = if (turnedOn) ComposeAppTheme.colors.dark else ComposeAppTheme.colors.jacob
                )
                Text(
                    text = title,
                    style = ComposeAppTheme.typography.captionSB,
                    color = if (turnedOn) ComposeAppTheme.colors.dark else ComposeAppTheme.colors.leah,
                )
            }
        },
    )
}