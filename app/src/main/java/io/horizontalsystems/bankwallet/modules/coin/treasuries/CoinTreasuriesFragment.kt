package com.mrv.wallet.modules.coin.treasuries

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.market.tvl.TvlModule
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.HSSwipeRefresh
import com.mrv.wallet.ui.compose.Select
import com.mrv.wallet.ui.compose.components.AlertGroup
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.ButtonSecondaryCircle
import com.mrv.wallet.ui.compose.components.CellFooter
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.HsImage
import com.mrv.wallet.ui.compose.components.ListErrorView
import com.mrv.wallet.ui.compose.components.MarketCoinFirstRow
import com.mrv.wallet.ui.compose.components.SectionItemBorderedRowUniversalClear
import com.mrv.wallet.ui.compose.components.SortMenu
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.subhead2_grey
import com.mrv.wallet.ui.compose.components.subhead2_jacob
import io.horizontalsystems.marketkit.models.Coin

class CoinTreasuriesFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Coin>(navController) { input ->
            CoinTreasuriesScreen(
                viewModel(factory = CoinTreasuriesModule.Factory(input))
            )
        }
    }

    @Composable
    private fun CoinTreasuriesScreen(
        viewModel: CoinTreasuriesViewModel
    ) {
        val viewState by viewModel.viewStateLiveData.observeAsState()
        val isRefreshing by viewModel.isRefreshingLiveData.observeAsState(false)
        val treasuriesData by viewModel.coinTreasuriesLiveData.observeAsState()
        val chainSelectorDialogState by viewModel.treasuryTypeSelectorDialogStateLiveData.observeAsState(
            TvlModule.SelectorDialogState.Closed
        )

        Scaffold(
            backgroundColor = ComposeAppTheme.colors.tyler,
            topBar = {
                AppBar(
                    title = stringResource(R.string.CoinPage_Treasuries),
                    navigationIcon = {
                        HsBackButton(onClick = { findNavController().popBackStack() })
                    }
                )
            }
        ) { paddingValues ->
            HSSwipeRefresh(
                refreshing = isRefreshing,
                modifier = Modifier.padding(paddingValues),
                onRefresh = {
                    viewModel.refresh()
                },
                content = {
                    Crossfade(viewState, label = "") { viewState ->
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
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    treasuriesData?.let { treasuriesData ->
                                        item {
                                            CoinTreasuriesMenu(
                                                treasuryTypeSelect = treasuriesData.treasuryTypeSelect,
                                                sortDescending = treasuriesData.sortDescending,
                                                onClickTreasuryTypeSelector = viewModel::onClickTreasuryTypeSelector,
                                                onToggleSortType = viewModel::onToggleSortType
                                            )
                                        }

                                        items(treasuriesData.coinTreasuries) { item ->
                                            SectionItemBorderedRowUniversalClear(
                                                borderBottom = true
                                            ) {
                                                HsImage(
                                                    url = item.fundLogoUrl,
                                                    modifier = Modifier
                                                        .padding(end = 16.dp)
                                                        .size(32.dp)
                                                )
                                                Column(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    MarketCoinFirstRow(item.fund, item.amount)
                                                    VSpacer(3.dp)
                                                    CoinTreasurySecondRow(
                                                        item.country,
                                                        item.amountInCurrency
                                                    )
                                                }
                                            }
                                        }

                                        item {
                                            VSpacer(32.dp)
                                            CellFooter(text = stringResource(id = R.string.CoinPage_Treasuries_PoweredBy))
                                        }
                                    }
                                }
                            }

                            null -> {}
                        }
                    }

                    // chain selector dialog
                    when (val option = chainSelectorDialogState) {
                        is CoinTreasuriesModule.SelectorDialogState.Opened -> {
                            AlertGroup(
                                stringResource(R.string.CoinPage_Treasuries_FilterTitle),
                                option.select,
                                viewModel::onSelectTreasuryType,
                                viewModel::onTreasuryTypeSelectorDialogDismiss
                            )
                        }
                    }
                }
            )
        }
    }

    @Composable
    private fun CoinTreasuriesMenu(
        treasuryTypeSelect: Select<CoinTreasuriesModule.TreasuryTypeFilter>,
        sortDescending: Boolean,
        onClickTreasuryTypeSelector: () -> Unit,
        onToggleSortType: () -> Unit
    ) {
        HeaderSorting(borderTop = true, borderBottom = true) {
            Box(modifier = Modifier.weight(1f)) {
                SortMenu(treasuryTypeSelect.selected.title, onClickTreasuryTypeSelector)
            }
            ButtonSecondaryCircle(
                modifier = Modifier.padding(end = 16.dp),
                icon = if (sortDescending) R.drawable.ic_sort_h2l_20 else R.drawable.ic_sort_l2h_20,
                onClick = { onToggleSortType() }
            )
        }
    }

    @Composable
    private fun CoinTreasurySecondRow(
        country: String,
        fiatAmount: String
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            subhead2_grey(
                text = country,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.weight(1f))
            subhead2_jacob(
                text = fiatAmount,
                maxLines = 1,
            )
        }
    }
}
