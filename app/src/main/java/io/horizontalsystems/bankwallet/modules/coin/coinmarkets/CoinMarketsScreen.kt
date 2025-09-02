package com.mrv.wallet.modules.coin.coinmarkets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.mrv.wallet.R
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.MarketTickerViewItem
import com.mrv.wallet.modules.coin.coinmarkets.CoinMarketsModule.ExchangeType
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.market.MarketDataValue
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.Select
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AlertGroup
import com.mrv.wallet.ui.compose.components.ButtonPrimaryDefaults
import com.mrv.wallet.ui.compose.components.ButtonSecondary
import com.mrv.wallet.ui.compose.components.ButtonSecondaryWithIcon
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.ListEmptyView
import com.mrv.wallet.ui.compose.components.ListErrorView
import com.mrv.wallet.ui.compose.components.MarketCoinFirstRow
import com.mrv.wallet.ui.compose.components.MarketCoinSecondRow
import com.mrv.wallet.ui.compose.components.SecondaryButtonDefaults
import com.mrv.wallet.ui.compose.components.SectionItemBorderedRowUniversalClear
import com.mrv.wallet.ui.helpers.LinkHelper
import io.horizontalsystems.marketkit.models.FullCoin
import kotlinx.coroutines.launch

@Composable
fun CoinMarketsScreen(
    fullCoin: FullCoin
) {
    val viewModel = viewModel<CoinMarketsViewModel>(factory = CoinMarketsModule.Factory(fullCoin))

    var scrollToTopAfterUpdate by rememberSaveable { mutableStateOf(false) }
    var showExchangeTypeSelector by rememberSaveable { mutableStateOf(false) }
    val uiState = viewModel.uiState

    Surface(color = ComposeAppTheme.colors.tyler) {
        Crossfade(uiState.viewState, label = "") { viewItemState ->
            when (viewItemState) {
                ViewState.Loading -> {
                    Loading()
                }

                is ViewState.Error -> {
                    ListErrorView(stringResource(R.string.SyncError), viewModel::onErrorClick)
                }

                ViewState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.items.isEmpty()) {
                            ListEmptyView(
                                text = stringResource(R.string.CoinPage_NoDataAvailable),
                                icon = R.drawable.ic_no_data
                            )
                        } else {
                            CoinMarketsMenu(
                                exchangeTypeMenu = uiState.exchangeTypeMenu,
                                verified = viewModel.verified,
                                showExchangeTypeSelector = { showExchangeTypeSelector = true },
                                onVerifiedEnabled = { verified ->
                                    viewModel.setVerified(verified)
                                    scrollToTopAfterUpdate = true
                                }
                            )
                            CoinMarketList(uiState.items, scrollToTopAfterUpdate)
                            if (scrollToTopAfterUpdate) {
                                scrollToTopAfterUpdate = false
                            }
                        }
                    }
                }
            }
        }
        if (showExchangeTypeSelector) {
            AlertGroup(
                title = stringResource(R.string.CoinPage_MarketsVerifiedMenu_ExchangeType),
                select = uiState.exchangeTypeMenu,
                onSelect = {
                    viewModel::setExchangeType.invoke(it)
                    showExchangeTypeSelector = false
                },
                onDismiss = { showExchangeTypeSelector = false }
            )
        }
    }
}

@Composable
fun CoinMarketsMenu(
    exchangeTypeMenu: Select<ExchangeType>,
    verified: Boolean,
    showExchangeTypeSelector: () -> Unit,
    onVerifiedEnabled: (Boolean) -> Unit,
) {

    HeaderSorting(borderTop = true, borderBottom = true) {
        ButtonSecondaryWithIcon(
            modifier = Modifier.padding(start = 16.dp),
            iconRight = painterResource(R.drawable.ic_down_arrow_20),
            title = exchangeTypeMenu.selected.title.getString(),
            onClick = showExchangeTypeSelector
        )
        Spacer(Modifier.weight(1f))
        TurnOnButton(
            modifier = Modifier.padding(end = 16.dp),
            title = stringResource(R.string.CoinPage_MarketsVerifiedMenu_Verified),
            turnedOn = verified,
            onToggle = { verified ->
                onVerifiedEnabled.invoke(verified)
            }
        )
    }
}

@Composable
fun CoinMarketList(
    items: List<MarketTickerViewItem>,
    scrollToTop: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        items(items) { item ->
            CoinMarketCell(
                item.market,
                item.pair,
                item.marketImageUrl ?: "",
                item.volumeToken,
                MarketDataValue.Volume(item.volumeFiat),
                item.tradeUrl,
                item.badge
            )
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
        if (scrollToTop) {
            coroutineScope.launch {
                listState.scrollToItem(0)
            }
        }
    }
}

@Composable
fun CoinMarketCell(
    name: String,
    subtitle: String,
    iconUrl: String,
    volumeToken: String,
    marketDataValue: MarketDataValue,
    tradeUrl: String?,
    badge: TranslatableString?
) {
    val context = LocalContext.current
    SectionItemBorderedRowUniversalClear(
        onClick = tradeUrl?.let {
            { LinkHelper.openLinkInAppBrowser(context, it) }
        },
        borderBottom = true
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = iconUrl,
                error = painterResource(R.drawable.ic_platform_placeholder_24)
            ),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            MarketCoinFirstRow(name, volumeToken, badge = badge?.getString())
            Spacer(modifier = Modifier.height(3.dp))
            MarketCoinSecondRow(subtitle, marketDataValue, null)
        }
    }
}

@Composable
fun TurnOnButton(
    modifier: Modifier,
    title: String,
    turnedOn: Boolean,
    onToggle: (Boolean) -> Unit
) {
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
        modifier = modifier,
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
                Text(
                    text = title,
                    style = ComposeAppTheme.typography.captionSB,
                    color = if (turnedOn) ComposeAppTheme.colors.dark else ComposeAppTheme.colors.leah,
                )
            }
        },
    )
}