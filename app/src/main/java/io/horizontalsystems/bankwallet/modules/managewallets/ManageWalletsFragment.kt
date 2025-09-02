package com.mrv.wallet.modules.managewallets

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.slideFromBottom
import com.mrv.wallet.core.slideFromBottomForResult
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.modules.enablecoin.restoresettings.RestoreSettingsViewModel
import com.mrv.wallet.modules.restoreaccount.restoreblockchains.CoinViewItem
import com.mrv.wallet.modules.restoreconfig.BirthdayHeightConfig
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.Badge
import com.mrv.wallet.ui.compose.components.HsDivider
import com.mrv.wallet.ui.compose.components.HsIconButton
import com.mrv.wallet.ui.compose.components.HsSwitch
import com.mrv.wallet.ui.compose.components.ListEmptyView
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.RowUniversal
import com.mrv.wallet.ui.compose.components.SearchBar
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.subhead2_grey
import io.horizontalsystems.marketkit.models.Token

class ManageWalletsFragment : BaseComposeFragment() {

    private val vmFactory by lazy { ManageWalletsModule.Factory() }
    private val viewModel by viewModels<ManageWalletsViewModel> { vmFactory }
    private val restoreSettingsViewModel by viewModels<RestoreSettingsViewModel> { vmFactory }

    @Composable
    override fun GetContent(navController: NavController) {
        ManageWalletsScreen(
            navController,
            viewModel,
            restoreSettingsViewModel
        )
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ManageWalletsScreen(
    navController: NavController,
    viewModel: ManageWalletsViewModel,
    restoreSettingsViewModel: RestoreSettingsViewModel
) {
    val coinItems by viewModel.viewItemsLiveData.observeAsState()

    restoreSettingsViewModel.openBirthdayHeightConfig?.let { token ->
        restoreSettingsViewModel.birthdayHeightConfigOpened()

        navController.slideFromBottomForResult<BirthdayHeightConfig.Result>(
            resId = R.id.zcashConfigure,
            input = token
        ) {
            if (it.config != null) {
                restoreSettingsViewModel.onEnter(it.config)
            } else {
                restoreSettingsViewModel.onCancelEnterBirthdayHeight()
            }
        }

        stat(page = StatPage.CoinManager, event = StatEvent.Open(StatPage.BirthdayInput))
    }

    Column(
        modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)
    ) {
        SearchBar(
            title = stringResource(R.string.ManageCoins_title),
            searchHintText = stringResource(R.string.ManageCoins_Search),
            menuItems = if (viewModel.addTokenEnabled) {
                listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.ManageCoins_AddToken),
                        icon = R.drawable.ic_add_24,
                        onClick = {
                            navController.slideFromRight(R.id.addTokenFragment)

                            stat(
                                page = StatPage.CoinManager,
                                event = StatEvent.Open(StatPage.AddToken)
                            )
                        }
                    ))
            } else {
                listOf()
            },
            onClose = { navController.popBackStack() },
            onSearchTextChanged = { text ->
                viewModel.updateFilter(text)
            }
        )

        coinItems?.let {
            if (it.isEmpty()) {
                ListEmptyView(
                    text = stringResource(R.string.ManageCoins_NoResults),
                    icon = R.drawable.ic_not_found
                )
            } else {
                LazyColumn {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        HsDivider()
                    }
                    items(it) { viewItem ->
                        CoinCell(
                            viewItem = viewItem,
                            onItemClick = {
                                if (viewItem.enabled) {
                                    viewModel.disable(viewItem.item)

                                    stat(
                                        page = StatPage.CoinManager,
                                        event = StatEvent.DisableToken(viewItem.item)
                                    )
                                } else {
                                    viewModel.enable(viewItem.item)

                                    stat(
                                        page = StatPage.CoinManager,
                                        event = StatEvent.EnableToken(viewItem.item)
                                    )
                                }
                            },
                            onInfoClick = {
                                navController.slideFromBottom(
                                    R.id.configuredTokenInfo,
                                    viewItem.item
                                )

                                stat(
                                    page = StatPage.CoinManager,
                                    event = StatEvent.OpenTokenInfo(viewItem.item)
                                )
                            }
                        )
                    }
                    item {
                        VSpacer(height = 32.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinCell(
    viewItem: CoinViewItem<Token>,
    onItemClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Column {
        RowUniversal(
            onClick = onItemClick,
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalPadding = 0.dp
        ) {
            Image(
                painter = viewItem.imageSource.painter(),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp, top = 12.dp, bottom = 12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    body_leah(
                        text = viewItem.title,
                        maxLines = 1,
                    )
                    viewItem.label?.let { labelText ->
                        Badge(
                            text = labelText,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
                subhead2_grey(
                    text = viewItem.subtitle,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            if (viewItem.hasInfo) {
                HsIconButton(onClick = onInfoClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info_20),
                        contentDescription = null,
                        tint = ComposeAppTheme.colors.grey
                    )
                }
            }
            HsSwitch(
                modifier = Modifier.padding(0.dp),
                checked = viewItem.enabled,
                onCheckedChange = { onItemClick.invoke() },
            )
        }
        HsDivider()
    }
}

//@Preview
//@Composable
//fun PreviewCoinCell() {
//    val viewItem = CoinViewItem(
//        item = "ethereum",
//        imageSource = ImageSource.Local(R.drawable.logo_ethereum_24),
//        title = "ETH",
//        subtitle = "Ethereum",
//        enabled = true,
//        hasSettings = true,
//        hasInfo = true,
//        label = "Ethereum"
//    )
//    ComposeAppTheme {
//        CoinCell(
//            viewItem,
//            {},
//            {},
//            {}
//        )
//    }
//}
