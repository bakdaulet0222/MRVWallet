package com.mrv.wallet.modules.market.earn

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.paidAction
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.slideFromRightForResult
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatSection
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.market.earn.vault.VaultFragment
import com.mrv.wallet.modules.market.topcoins.OptionController
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.HSSwipeRefresh
import com.mrv.wallet.ui.compose.Select
import com.mrv.wallet.ui.compose.components.AlertGroup
import com.mrv.wallet.ui.compose.components.Badge
import com.mrv.wallet.ui.compose.components.ButtonPrimaryYellow
import com.mrv.wallet.ui.compose.components.ButtonSecondaryWithIcon
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.HsDivider
import com.mrv.wallet.ui.compose.components.HsImage
import com.mrv.wallet.ui.compose.components.ListEmptyView
import com.mrv.wallet.ui.compose.components.ListErrorView
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.diffColor
import com.mrv.wallet.ui.compose.components.headline2_leah
import com.mrv.wallet.ui.compose.components.subhead2_grey
import io.horizontalsystems.subscriptions.core.AdvancedSearch
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun MarketEarnScreen(
    navController: NavController
) {
    val viewModel = viewModel<MarketEarnViewModel>(factory = EarnModule.Factory())
    val uiState = viewModel.uiState
    var openFilterSelector by rememberSaveable { mutableStateOf(false) }
    var openPeriodSelector by rememberSaveable { mutableStateOf(false) }
    var openSortingSelector by rememberSaveable { mutableStateOf(false) }
    var scrollToTopAfterUpdate by rememberSaveable { mutableStateOf(false) }

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
                    if (uiState.items.isEmpty()) {
                        ListEmptyView(
                            text = stringResource(R.string.Error),
                            icon = R.drawable.ic_sync_error
                        )
                    } else {
                        VaultList(
                            noPremium = uiState.noPremium,
                            items = uiState.items,
                            blurredItems = uiState.blurredItems,
                            scrollToTop = scrollToTopAfterUpdate,
                            onCoinClick = { viewItem ->
                                val input = VaultFragment.Input(
                                    rank = viewItem.rank,
                                    address = viewItem.address,
                                    name = viewItem.name,
                                    tvl = viewItem.tvl,
                                    chain = viewItem.blockchainName,
                                    url = viewItem.url,
                                    holders = viewItem.holders,
                                    assetSymbol = viewItem.assetSymbol,
                                    protocolName = viewItem.protocolName,
                                    assetLogo = viewItem.assetLogo
                                )
                                navController.paidAction(AdvancedSearch) {
                                    navController.slideFromRight(R.id.vaultFragment, input)
                                }
                            },
                            onGetPremiumClick = {
                                navController.paidAction(AdvancedSearch) {
                                    //refresh page
                                }
                            },
                            preItems = {
                                stickyHeader {
                                    HeaderSorting(
                                        borderBottom = true,
                                    ) {
                                        HSpacer(width = 16.dp)
                                        OptionController(
                                            uiState.filterBy.titleResId,
                                            onOptionClick = {
                                                openFilterSelector = true
                                            }
                                        )
                                        HSpacer(width = 12.dp)
                                        ButtonSecondaryWithIcon(
                                            modifier = Modifier.height(28.dp),
                                            onClick = {
                                                openSortingSelector = true
                                            },
                                            title = uiState.sortingByTitle,
                                            iconRight = painterResource(R.drawable.ic_down_arrow_20),
                                        )
                                        HSpacer(width = 12.dp)
                                        ButtonSecondaryWithIcon(
                                            modifier = Modifier.height(28.dp),
                                            onClick = {
                                                openPeriodSelector = true
                                            },
                                            title = stringResource(uiState.apyPeriod.titleResId),
                                            iconRight = painterResource(R.drawable.ic_down_arrow_20),
                                        )
                                        HSpacer(width = 12.dp)
                                        ButtonSecondaryWithIcon(
                                            modifier = Modifier.height(28.dp),
                                            onClick = {
                                                navController.slideFromRightForResult<VaultBlockchainsSelectorFragment.Result>(
                                                    R.id.vaultsBlockchainsSelectorFragment,
                                                    VaultBlockchainsSelectorFragment.Input(
                                                        uiState.selectedBlockchains,
                                                        uiState.blockchains
                                                    )
                                                ) {
                                                    viewModel.onBlockchainsSelected(it.selected)
                                                }
                                            },
                                            title = uiState.chainSelectorMenuTitle,
                                            iconRight = painterResource(R.drawable.ic_down_arrow_20),
                                        )
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

    if (openFilterSelector) {
        AlertGroup(
            title = stringResource(R.string.Market_Filter_PopupTitle),
            select = Select(uiState.filterBy, viewModel.filterOptions),
            onSelect = { selected ->
                openFilterSelector = false
                navController.paidAction(AdvancedSearch) {
                    scrollToTopAfterUpdate = true
                    viewModel.onFilterBySelected(selected)
                }
            },
            onDismiss = {
                openFilterSelector = false
            }
        )
    }
    if (openPeriodSelector) {
        AlertGroup(
            title = stringResource(R.string.CoinPage_Period),
            select = Select(uiState.apyPeriod, viewModel.apyPeriods),
            onSelect = { selected ->
                openPeriodSelector = false
                navController.paidAction(AdvancedSearch) {
                    scrollToTopAfterUpdate = true
                    viewModel.onApyPeriodSelected(selected)
                }
            },
            onDismiss = {
                openPeriodSelector = false
            }
        )
    }
    if (openSortingSelector) {
        AlertGroup(
            title = stringResource(R.string.Market_Sort_PopupTitle),
            select = Select(uiState.sortingBy, viewModel.sortingOptions),
            onSelect = { selected ->
                openSortingSelector = false
                navController.paidAction(AdvancedSearch) {
                    scrollToTopAfterUpdate = true
                    viewModel.onSortingSelected(selected)
                }
            },
            onDismiss = {
                openSortingSelector = false
            }
        )
    }
}

@Composable
fun VaultList(
    noPremium: Boolean,
    items: List<EarnModule.VaultViewItem>,
    blurredItems: List<EarnModule.VaultViewItem>,
    scrollToTop: Boolean,
    onCoinClick: (EarnModule.VaultViewItem) -> Unit,
    onGetPremiumClick: () -> Unit = {},
    preItems: LazyListScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        preItems.invoke(this)
        items(
            items = items,
            key = { item -> item.address + item.protocolName }
        ) { item ->
            VaultItem(
                title = item.assetSymbol,
                subtitle = item.name,
                coinIconUrl = item.assetLogo,
                coinIconPlaceholder = R.drawable.coin_placeholder,
                value = item.apy,
                subvalue = item.tvl,
                label = item.blockchainName,
                onClick = { onCoinClick.invoke(item) },
            )

            HsDivider()
        }
        item {
            if (noPremium) {
                PremiumContentMessage(blurredItems) {
                    onGetPremiumClick.invoke()
                }
            }
        }
        item {
            //Add bottom space only when all items are visible
            //and don't show bottom space when Premium banner is shown
            if (!noPremium) {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        if (scrollToTop) {
            coroutineScope.launch {
                listState.scrollToItem(0)
            }
        }
    }
}

@Composable
private fun VaultItem(
    title: String,
    subtitle: String,
    coinIconUrl: String?,
    alternativeCoinIconUrl: String? = null,
    coinIconPlaceholder: Int,
    value: BigDecimal,
    subvalue: String,
    label: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 24.dp)
            .clickable { onClick?.invoke() }
            .background(ComposeAppTheme.colors.tyler)
            .padding(horizontal = 16.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HsImage(
            url = coinIconUrl,
            alternativeUrl = alternativeCoinIconUrl,
            placeholder = coinIconPlaceholder,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(32.dp)
                .clip(CircleShape)
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            VaultFirstRow(title, value, label)
            Spacer(modifier = Modifier.height(3.dp))
            VaultSecondRow(subtitle, subvalue)
        }
    }
}

@Composable
fun VaultFirstRow(
    title: String,
    value: BigDecimal,
    badge: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            body_leah(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (badge != null) {
                Badge(
                    text = badge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Text(
            text = "APY ${vaultDiffText(value)}",
            color = diffColor(value),
            style = ComposeAppTheme.typography.subheadR,
            maxLines = 1,
        )
    }
}

@Composable
private fun VaultSecondRow(
    subtitle: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        subhead2_grey(
            text = subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        subhead2_grey(
            text = "TVL: $value",
            maxLines = 1,
        )
    }
}

@Composable
private fun PremiumContentMessage(
    blurredItems: List<EarnModule.VaultViewItem>,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.blur(
                radiusX = 16.dp,
                radiusY = 16.dp
            )
        ) {
            blurredItems.forEach { item ->
                VaultItem(
                    title = item.assetSymbol,
                    subtitle = item.name,
                    coinIconUrl = item.assetLogo,
                    alternativeCoinIconUrl = null,
                    coinIconPlaceholder = R.drawable.coin_placeholder,
                    value = item.apy,
                    subvalue = item.tvl,
                    label = item.blockchainName,
                )
                HsDivider()
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                painter = painterResource(R.drawable.icon_lock_48),
                contentDescription = "lock icon",
                tint = ComposeAppTheme.colors.grey
            )

            VSpacer(24.dp)
            headline2_leah(
                modifier = Modifier.padding(horizontal = 48.dp),
                text = stringResource(R.string.Market_Vaults_WantToUnlockPremium),
                textAlign = TextAlign.Center,
            )
            VSpacer(24.dp)
            ButtonPrimaryYellow(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .fillMaxWidth(),
                title = stringResource(R.string.Market_Vaults_UnlockPremium),
                onClick = onClick
            )
        }
    }
}

private fun vaultDiffText(diff: BigDecimal): String {
    return App.numberFormatter.format(diff.abs(), 0, 2, "", "%")
}