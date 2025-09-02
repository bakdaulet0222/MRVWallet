package com.mrv.wallet.modules.coin

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statTab
import com.mrv.wallet.modules.coin.analytics.CoinAnalyticsScreen
import com.mrv.wallet.modules.coin.coinmarkets.CoinMarketsScreen
import com.mrv.wallet.modules.coin.overview.ui.CoinOverviewScreen
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.ListEmptyView
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.TabItem
import com.mrv.wallet.ui.compose.components.Tabs
import io.horizontalsystems.core.helpers.HudHelper
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class CoinFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Input>(navController) { input ->
            CoinScreen(
                input.coinUid,
                coinViewModel(input.coinUid),
                navController,
                childFragmentManager
            )
        }
    }

    private fun coinViewModel(coinUid: String): CoinViewModel? = try {
        val viewModel by navGraphViewModels<CoinViewModel>(R.id.coinFragment) {
            CoinModule.Factory(coinUid)
        }
        viewModel
    } catch (e: Exception) {
        null
    }

    @Parcelize
    data class Input(val coinUid: String) : Parcelable
}

@Composable
fun CoinScreen(
    coinUid: String,
    coinViewModel: CoinViewModel?,
    navController: NavController,
    fragmentManager: FragmentManager
) {
    if (coinViewModel != null) {
        CoinTabs(coinViewModel, navController, fragmentManager)
    } else {
        CoinNotFound(coinUid, navController)
    }
}

@Composable
fun CoinTabs(
    viewModel: CoinViewModel,
    navController: NavController,
    fragmentManager: FragmentManager
) {
    val tabs = viewModel.tabs
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = viewModel.fullCoin.coin.code,
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
                menuItems = buildList {
                    if (viewModel.isWatchlistEnabled) {
                        if (viewModel.isFavorite) {
                            add(
                                MenuItem(
                                    title = TranslatableString.ResString(R.string.CoinPage_Unfavorite),
                                    icon = R.drawable.ic_heart_filled_24,
                                    tint = ComposeAppTheme.colors.jacob,
                                    onClick = {
                                        viewModel.onUnfavoriteClick()

                                        stat(
                                            page = StatPage.CoinPage,
                                            event = StatEvent.RemoveFromWatchlist(viewModel.fullCoin.coin.uid)
                                        )
                                    }
                                )
                            )
                        } else {
                            add(
                                MenuItem(
                                    title = TranslatableString.ResString(R.string.CoinPage_Favorite),
                                    icon = R.drawable.ic_heart_24,
                                    tint = ComposeAppTheme.colors.grey,
                                    onClick = {
                                        viewModel.onFavoriteClick()

                                        stat(
                                            page = StatPage.CoinPage,
                                            event = StatEvent.AddToWatchlist(viewModel.fullCoin.coin.uid)
                                        )
                                    }
                                )
                            )
                        }
                    }
                }
            )
        }
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .padding(innerPaddings)
                .navigationBarsPadding()
        ) {
            val selectedTab = tabs[pagerState.currentPage]
            val tabItems = tabs.map {
                TabItem(stringResource(id = it.titleResId), it == selectedTab, it)
            }
            Tabs(tabItems, onClick = { tab ->
                coroutineScope.launch {
                    pagerState.scrollToPage(tab.ordinal)

                    stat(page = StatPage.CoinPage, event = StatEvent.SwitchTab(tab.statTab))
                }
            })

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                when (tabs[page]) {
                    CoinModule.Tab.Overview -> {
                        CoinOverviewScreen(
                            fullCoin = viewModel.fullCoin,
                            navController = navController
                        )
                    }

                    CoinModule.Tab.Market -> {
                        CoinMarketsScreen(fullCoin = viewModel.fullCoin)
                    }

                    CoinModule.Tab.Details -> {
                        CoinAnalyticsScreen(
                            fullCoin = viewModel.fullCoin,
                            navController = navController,
                            fragmentManager = fragmentManager
                        )
                    }
                }
            }

            viewModel.successMessage?.let {
                HudHelper.showSuccessMessage(view, it)

                viewModel.onSuccessMessageShown()
            }
        }
    }
}

@Composable
fun CoinNotFound(coinUid: String, navController: NavController) {
    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = coinUid,
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                }
            )
        },
        content = {
            ListEmptyView(
                paddingValues = it,
                text = stringResource(R.string.CoinPage_CoinNotFound, coinUid),
                icon = R.drawable.ic_not_available
            )
        }
    )
}
