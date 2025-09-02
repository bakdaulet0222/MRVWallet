package com.mrv.wallet.modules.market.toppairs

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrv.wallet.R
import com.mrv.wallet.core.fiatIconUrl
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatSection
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.market.MarketDataValue
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.HSSwipeRefresh
import com.mrv.wallet.ui.compose.components.ButtonSecondaryWithIcon
import com.mrv.wallet.ui.compose.components.CoinImage
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.HsImage
import com.mrv.wallet.ui.compose.components.ListErrorView
import com.mrv.wallet.ui.compose.components.MarketCoinFirstRow
import com.mrv.wallet.ui.compose.components.MarketCoinSecondRow
import com.mrv.wallet.ui.compose.components.SectionItemBorderedRowUniversalClear
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.helpers.LinkHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopPairsScreen() {
    val viewModel = viewModel<TopPairsViewModel>(factory = TopPairsViewModel.Factory())
    val uiState = viewModel.uiState
    val context = LocalContext.current

    val state = rememberSaveable(uiState.sortDescending, saver = LazyListState.Saver) {
        LazyListState(0, 0)
    }

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
    ) {
        Column(modifier = Modifier.padding(it)) {
            HSSwipeRefresh(
                topPadding = 44,
                refreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh
            ) {
                Crossfade(uiState.viewState, label = "") { viewState ->
                    when (viewState) {
                        ViewState.Loading -> {
                            Loading()
                        }

                        is ViewState.Error -> {
                            ListErrorView(
                                stringResource(R.string.SyncError),
                                viewModel::onErrorClick
                            )
                        }

                        ViewState.Success -> {
                            LazyColumn(
                                state = state,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                stickyHeader {
                                    HeaderSorting(borderBottom = true) {
                                        HSpacer(width = 16.dp)
                                        ButtonSecondaryWithIcon(
                                            modifier = Modifier.height(28.dp),
                                            onClick = {
                                                viewModel.toggleSorting()
                                            },
                                            title = stringResource(R.string.Market_Volume),
                                            iconRight = painterResource(
                                                if (uiState.sortDescending) R.drawable.ic_arrow_down_20 else R.drawable.ic_arrow_up_20
                                            ),
                                        )
                                        HSpacer(width = 16.dp)
                                    }
                                }
                                itemsIndexed(uiState.items) { _, item ->
                                    TopPairItem(item, borderBottom = true) {
                                        it.tradeUrl?.let {
                                            LinkHelper.openLinkInAppBrowser(context, it)

                                            stat(
                                                page = StatPage.Markets,
                                                event = StatEvent.Open(StatPage.ExternalMarketPair),
                                                section = StatSection.Pairs
                                            )
                                        }
                                    }
                                }
                                item {
                                    VSpacer(height = 32.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopPairItem(
    item: TopPairViewItem,
    borderTop: Boolean = false,
    borderBottom: Boolean = false,
    onItemClick: (TopPairViewItem) -> Unit,
) {
    SectionItemBorderedRowUniversalClear(
        borderTop = borderTop,
        borderBottom = borderBottom,
        onClick = { onItemClick(item) }
    ) {
        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .width(54.dp)
        ) {

            val targetCoinModifier = Modifier
                .size(32.dp)
                .background(ComposeAppTheme.colors.tyler)
                .clip(CircleShape)
                .align(Alignment.TopEnd)

            if (item.targetCoin != null) {
                CoinImage(
                    coin = item.targetCoin,
                    modifier = targetCoinModifier
                )
            } else {
                HsImage(
                    url = item.target.fiatIconUrl,
                    placeholder = R.drawable.ic_platform_placeholder_32,
                    modifier = targetCoinModifier
                )
            }

            val baseCoinModifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ComposeAppTheme.colors.tyler)
                .align(Alignment.TopStart)

            if (item.baseCoin != null) {
                CoinImage(
                    coin = item.baseCoin,
                    modifier = baseCoinModifier
                )
            } else {
                HsImage(
                    url = item.base.fiatIconUrl,
                    placeholder = R.drawable.ic_platform_placeholder_32,
                    modifier = baseCoinModifier
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            MarketCoinFirstRow(item.title, item.volumeInFiat)
            Spacer(modifier = Modifier.height(3.dp))
            MarketCoinSecondRow(
                subtitle = item.name,
                marketDataValue = item.price?.let { MarketDataValue.Volume(it) },
                label = item.rank
            )
        }
    }
}