package com.mrv.wallet.modules.market.topplatforms

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatSection
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statPeriod
import com.mrv.wallet.core.stats.statSortType
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.market.MarketDataValue
import com.mrv.wallet.modules.market.SortingField
import com.mrv.wallet.modules.market.TimeDuration
import com.mrv.wallet.modules.market.topcoins.OptionController
import com.mrv.wallet.ui.compose.HSSwipeRefresh
import com.mrv.wallet.ui.compose.Select
import com.mrv.wallet.ui.compose.components.AlertGroup
import com.mrv.wallet.ui.compose.components.BadgeWithDiff
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.HsImage
import com.mrv.wallet.ui.compose.components.ListErrorView
import com.mrv.wallet.ui.compose.components.MarketCoinFirstRow
import com.mrv.wallet.ui.compose.components.MarketDataValueComponent
import com.mrv.wallet.ui.compose.components.SectionItemBorderedRowUniversalClear
import com.mrv.wallet.ui.compose.components.subhead2_grey
import java.math.BigDecimal

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopPlatforms(
    navController: NavController,
    viewModel: TopPlatformsViewModel = viewModel(
        factory = TopPlatformsModule.Factory(null)
    ),
) {
    var openPeriodSelector by rememberSaveable { mutableStateOf(false) }
    var openSortingSelector by rememberSaveable { mutableStateOf(false) }
    val uiState = viewModel.uiState

    Column {
        HSSwipeRefresh(
            refreshing = uiState.isRefreshing,
            topPadding = 44,
            onRefresh = {
                viewModel.refresh()

                stat(
                    page = StatPage.Markets,
                    event = StatEvent.Refresh,
                    section = StatSection.Platforms
                )
            }
        ) {
            Crossfade(uiState.viewState, label = "") { state ->
                when (state) {
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
                        if (uiState.viewItems.isNotEmpty()) {
                            TopPlatformsList(
                                viewItems = uiState.viewItems,
                                sortingField = uiState.sortingField,
                                timeDuration = uiState.timePeriod,
                                onItemClick = {
                                    navController.slideFromRight(
                                        R.id.marketPlatformFragment,
                                        it
                                    )

                                    stat(
                                        page = StatPage.Markets,
                                        event = StatEvent.OpenPlatform(it.uid),
                                        section = StatSection.Platforms
                                    )
                                },
                                preItems = {
                                    stickyHeader {
                                        HeaderSorting(borderBottom = true) {
                                            HSpacer(width = 16.dp)
                                            OptionController(
                                                uiState.sortingField.titleResId,
                                                onOptionClick = {
                                                    openSortingSelector = true
                                                }
                                            )
                                            HSpacer(width = 12.dp)
                                            OptionController(
                                                uiState.timePeriod.titleResId,
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
        }
    }
    //Dialogs
    if (openPeriodSelector) {
        AlertGroup(
            stringResource(R.string.CoinPage_Period),
            Select(uiState.timePeriod, viewModel.periods),
            { selected ->
                viewModel.onTimePeriodSelect(selected)
                openPeriodSelector = false
                stat(
                    page = StatPage.Markets,
                    event = StatEvent.SwitchPeriod(selected.statPeriod),
                    section = StatSection.Platforms
                )
            },
            { openPeriodSelector = false }
        )
    }
    if (openSortingSelector) {
        AlertGroup(
            stringResource(R.string.Market_Sort_PopupTitle),
            Select(uiState.sortingField, viewModel.sortingOptions),
            { selected ->
                viewModel.onSelectSortingField(selected)
                openSortingSelector = false
                stat(
                    page = StatPage.Markets,
                    event = StatEvent.SwitchSortType(selected.statSortType),
                    section = StatSection.Platforms
                )
            },
            { openSortingSelector = false }
        )
    }
}

@Composable
private fun TopPlatformsList(
    viewItems: List<TopPlatformViewItem>,
    sortingField: SortingField,
    timeDuration: TimeDuration,
    onItemClick: (Platform) -> Unit,
    preItems: LazyListScope.() -> Unit
) {
    val state = rememberSaveable(sortingField, timeDuration, saver = LazyListState.Saver) {
        LazyListState(0, 0)
    }

    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize()
    ) {
        preItems.invoke(this)
        items(viewItems) { item ->
            TopPlatformItem(item, onItemClick)
        }
    }
}

@Composable
private fun TopPlatformSecondRow(
    subtitle: String,
    marketDataValue: MarketDataValue?,
    rank: String,
    rankDiff: BigDecimal?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgeWithDiff(
            modifier = Modifier.padding(end = 8.dp),
            text = rank,
            diff = rankDiff
        )
        subhead2_grey(
            text = subtitle,
            maxLines = 1,
        )
        marketDataValue?.let {
            Spacer(modifier = Modifier.weight(1f))
            MarketDataValueComponent(marketDataValue)
        }
    }
}

@Composable
fun TopPlatformItem(item: TopPlatformViewItem, onItemClick: (Platform) -> Unit) {
    SectionItemBorderedRowUniversalClear(
        borderBottom = true,
        onClick = { onItemClick(item.platform) }
    ) {
        HsImage(
            url = item.iconUrl,
            placeholder = item.iconPlaceHolder,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(32.dp)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            MarketCoinFirstRow(item.platform.name, item.marketCap)
            Spacer(modifier = Modifier.height(3.dp))
            TopPlatformSecondRow(
                subtitle = item.subtitle,
                marketDataValue = MarketDataValue.Diff(item.marketCapDiff),
                rank = item.rank.toString(),
                rankDiff = item.rankDiff?.toBigDecimal()
            )
        }
    }
}