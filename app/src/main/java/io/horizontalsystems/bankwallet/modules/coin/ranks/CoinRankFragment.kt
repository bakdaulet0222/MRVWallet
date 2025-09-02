package com.mrv.wallet.modules.coin.ranks

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statPage
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.CoinFragment
import com.mrv.wallet.modules.coin.analytics.CoinAnalyticsModule.RankType
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.Select
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.ButtonSecondaryCircle
import com.mrv.wallet.ui.compose.components.ButtonSecondaryToggle
import com.mrv.wallet.ui.compose.components.DescriptionCard
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderSorting
import com.mrv.wallet.ui.compose.components.HsDivider
import com.mrv.wallet.ui.compose.components.ListErrorView
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.RowUniversal
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.captionSB_grey
import com.mrv.wallet.ui.compose.components.headline2_leah
import com.mrv.wallet.ui.compose.components.subhead2_grey

class CoinRankFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<RankType>(navController) { type ->
            CoinRankScreen(type, navController)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CoinRankScreen(
    type: RankType,
    navController: NavController,
    viewModel: CoinRankViewModel = viewModel(
        factory = CoinRankModule.Factory(type)
    )
) {
    val uiState = viewModel.uiState
    val viewItems = viewModel.uiState.rankViewItems

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Close),
                        icon = R.drawable.ic_close,
                        onClick = { navController.popBackStack() }
                    )
                )
            )
        }
    ) { padding ->
        Crossfade(
            targetState = uiState.viewState,
            modifier = Modifier.padding(padding),
            label = ""
        ) { viewItemState ->
            when (viewItemState) {
                ViewState.Loading -> {
                    Loading()
                }

                is ViewState.Error -> {
                    ListErrorView(stringResource(R.string.SyncError), viewModel::onErrorClick)
                }

                ViewState.Success -> {
                    var periodSelect by remember { mutableStateOf(uiState.periodSelect) }
                    val listState = rememberSaveable(
                        uiState.periodSelect?.selected,
                        uiState.sortDescending,
                        saver = LazyListState.Saver
                    ) {
                        LazyListState()
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                    ) {
                        item {
                            uiState.header.let { header ->
                                DescriptionCard(header.title, header.description, header.icon)
                            }
                        }

                        stickyHeader {
                            HeaderSorting {
                                ButtonSecondaryCircle(
                                    modifier = Modifier.padding(start = 16.dp),
                                    icon = if (uiState.sortDescending) R.drawable.ic_sort_l2h_20 else R.drawable.ic_sort_h2l_20,
                                    onClick = { viewModel.toggleSortType() }
                                )
                                Spacer(Modifier.weight(1f))
                                periodSelect?.let {
                                    ButtonSecondaryToggle(
                                        modifier = Modifier.padding(end = 16.dp),
                                        select = it,
                                        onSelect = { selectedDuration ->
                                            viewModel.toggle(selectedDuration)
                                            periodSelect = Select(selectedDuration, it.options)
                                        }
                                    )
                                }
                            }
                        }
                        coinRankList(viewItems, type, navController)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.coinRankList(
    items: List<CoinRankModule.RankViewItem>,
    type: RankType,
    navController: NavController
) {
    item {
        HsDivider()
    }
    items(items) { item ->
        CoinRankCell(
            rank = item.rank,
            name = item.title,
            subtitle = item.subTitle,
            iconUrl = item.iconUrl,
            value = item.value,
            onClick = {
                val arguments = CoinFragment.Input(item.coinUid)
                navController.slideFromRight(R.id.coinFragment, arguments)

                stat(page = type.statPage, event = StatEvent.OpenCoin(item.coinUid))
            }
        )
    }
    item {
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CoinRankCell(
    rank: String,
    name: String,
    subtitle: String,
    iconUrl: String?,
    value: String? = null,
    onClick: () -> Unit = {}
) {
    Column {
        RowUniversal(
            onClick = onClick,
        ) {
            captionSB_grey(
                modifier = Modifier.width(56.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                text = rank
            )
            Image(
                painter = rememberAsyncImagePainter(
                    model = iconUrl,
                    error = painterResource(R.drawable.coin_placeholder)
                ),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                headline2_leah(
                    text = name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                VSpacer(1.dp)
                subhead2_grey(
                    text = subtitle,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            value?.let {
                HSpacer(8.dp)
                body_leah(text = it)
            }
            HSpacer(16.dp)
        }
        HsDivider()
    }
}
