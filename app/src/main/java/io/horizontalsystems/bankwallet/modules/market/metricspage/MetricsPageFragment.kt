package com.mrv.wallet.modules.market.metricspage

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.alternativeImageUrl
import com.mrv.wallet.core.iconPlaceholder
import com.mrv.wallet.core.imageUrl
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statPage
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.chart.ChartViewModel
import com.mrv.wallet.modules.coin.CoinFragment
import com.mrv.wallet.modules.coin.overview.ui.Chart
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.metricchart.MetricsType
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.HSSwipeRefresh
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.ButtonSecondaryWithIcon
import com.mrv.wallet.ui.compose.components.DescriptionCard
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.ListErrorView
import com.mrv.wallet.ui.compose.components.MarketCoinClear
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.hsRememberLazyListState

class MetricsPageFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<MetricsType>(navController) { metricsType ->
            val factory = MetricsPageModule.Factory(metricsType)
            val chartViewModel by viewModels<ChartViewModel> { factory }
            val viewModel by viewModels<MetricsPageViewModel> { factory }
            MetricsPage(viewModel, chartViewModel, navController) {
                onCoinClick(it, navController)

                stat(page = metricsType.statPage, event = StatEvent.OpenCoin(it))
            }
        }
    }

    private fun onCoinClick(coinUid: String, navController: NavController) {
        val arguments = CoinFragment.Input(coinUid)

        navController.slideFromRight(R.id.coinFragment, arguments)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun MetricsPage(
        viewModel: MetricsPageViewModel,
        chartViewModel: ChartViewModel,
        navController: NavController,
        onCoinClick: (String) -> Unit,
    ) {
        val uiState = viewModel.uiState

        Column(
            Modifier
                .background(color = ComposeAppTheme.colors.tyler)
                .navigationBarsPadding()
        ) {
            AppBar(
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Close),
                        icon = R.drawable.ic_close,
                        onClick = {
                            navController.popBackStack()
                        }
                    )
                )
            )

            HSSwipeRefresh(
                refreshing = uiState.isRefreshing,
                onRefresh = {
                    viewModel.refresh()
                    chartViewModel.refresh()
                }
            ) {
                Crossfade(uiState.viewState, label = "") { viewState ->
                    when (viewState) {
                        ViewState.Loading -> {
                            Loading()
                        }

                        is ViewState.Error -> {
                            ListErrorView(
                                errorText = stringResource(R.string.SyncError),
                                onClick = {
                                    viewModel.onErrorClick()
                                    chartViewModel.refresh()
                                }
                            )
                        }

                        ViewState.Success -> {
                            val listState = hsRememberLazyListState(2, uiState.sortDescending)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = listState,
                                contentPadding = PaddingValues(bottom = 32.dp),
                            ) {
                                item {
                                    uiState.header.let { header ->
                                        DescriptionCard(
                                            header.title,
                                            header.description,
                                            header.icon
                                        )
                                    }
                                }
                                item {
                                    Chart(chartViewModel = chartViewModel)
                                }
                                stickyHeader {
                                    HeaderSorting(borderBottom = true, borderTop = true) {
                                        HSpacer(width = 16.dp)
                                        ButtonSecondaryWithIcon(
                                            modifier = Modifier.height(28.dp),
                                            onClick = {
                                                viewModel.toggleSorting()
                                            },
                                            title =uiState.toggleButtonTitle,
                                            iconRight = painterResource(
                                                if (uiState.sortDescending) R.drawable.ic_arrow_down_20 else R.drawable.ic_arrow_up_20
                                            ),
                                        )
                                        HSpacer(width = 16.dp)
                                    }
                                }
                                items(uiState.viewItems) { viewItem ->
                                    MarketCoinClear(
                                        title = viewItem.fullCoin.coin.code,
                                        subtitle = viewItem.subtitle,
                                        coinIconUrl = viewItem.fullCoin.coin.imageUrl,
                                        alternativeCoinIconUrl = viewItem.fullCoin.coin.alternativeImageUrl,
                                        coinIconPlaceholder = viewItem.fullCoin.iconPlaceholder,
                                        value = viewItem.coinRate,
                                        marketDataValue = viewItem.marketDataValue,
                                        label = viewItem.rank,
                                    ) { onCoinClick(viewItem.fullCoin.coin.uid) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
