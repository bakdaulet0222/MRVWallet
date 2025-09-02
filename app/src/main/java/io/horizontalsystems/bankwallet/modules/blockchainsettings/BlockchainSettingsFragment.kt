package com.mrv.wallet.modules.blockchainsettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.slideFromBottom
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.RowUniversal
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.subhead2_grey

class BlockchainSettingsFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        BlockchainSettingsScreen(
            navController = navController,
        )
    }

}

@Composable
private fun BlockchainSettingsScreen(
    navController: NavController,
    viewModel: BlockchainSettingsViewModel = viewModel(factory = BlockchainSettingsModule.Factory()),
) {

    Surface(color = ComposeAppTheme.colors.tyler) {
        Column(
            modifier = Modifier.navigationBarsPadding()
        ) {
            AppBar(
                title = stringResource(R.string.BlockchainSettings_Title),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(12.dp))
                BlockchainSettingsBlock(
                    btcLikeChains = viewModel.btcLikeChains,
                    otherChains = viewModel.otherChains,
                    navController = navController
                )
                Spacer(Modifier.height(44.dp))
            }
        }
    }

}

@Composable
fun BlockchainSettingsBlock(
    btcLikeChains: List<BlockchainSettingsModule.BlockchainViewItem>,
    otherChains: List<BlockchainSettingsModule.BlockchainViewItem>,
    navController: NavController
) {
    CellUniversalLawrenceSection(btcLikeChains) { item ->
        BlockchainSettingCell(item) {
            onClick(item, navController)
        }
    }
    Spacer(Modifier.height(32.dp))
    CellUniversalLawrenceSection(otherChains) { item ->
        BlockchainSettingCell(item) {
            onClick(item, navController)
        }
    }
}

private fun onClick(
    item: BlockchainSettingsModule.BlockchainViewItem,
    navController: NavController
) {
    when (item.blockchainItem) {
        is BlockchainSettingsModule.BlockchainItem.Btc -> {
            navController.slideFromBottom(R.id.btcBlockchainSettingsFragment, item.blockchainItem.blockchain)

            stat(
                page = StatPage.BlockchainSettings,
                event = StatEvent.OpenBlockchainSettingsBtc(item.blockchainItem.blockchain.uid)
            )
        }

        is BlockchainSettingsModule.BlockchainItem.Evm -> {
            navController.slideFromBottom(R.id.evmNetworkFragment, item.blockchainItem.blockchain)

            stat(
                page = StatPage.BlockchainSettings,
                event = StatEvent.OpenBlockchainSettingsEvm(item.blockchainItem.blockchain.uid)
            )
        }

        is BlockchainSettingsModule.BlockchainItem.Solana -> {
            navController.slideFromBottom(R.id.solanaNetworkFragment)

            stat(
                page = StatPage.BlockchainSettings,
                event = StatEvent.Open(StatPage.BlockchainSettingsSolana)
            )
        }

        is BlockchainSettingsModule.BlockchainItem.Monero -> {
            navController.slideFromBottom(R.id.moneroNetworkFragment)

            stat(
                page = StatPage.BlockchainSettings,
                event = StatEvent.OpenBlockchainSettingsEvm(item.blockchainItem.blockchain.uid)
            )
        }
    }
}

@Composable
private fun BlockchainSettingCell(
    item: BlockchainSettingsModule.BlockchainViewItem,
    onClick: () -> Unit
) {
    RowUniversal(
        onClick = onClick
    ) {
        Image(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(32.dp),
            painter = rememberAsyncImagePainter(
                model = item.imageUrl,
                error = painterResource(R.drawable.ic_platform_placeholder_32)
            ),
            contentDescription = null,
        )
        Column(modifier = Modifier.weight(1f)) {
            body_leah(text = item.title)
            subhead2_grey(text = item.subtitle)
        }
        Icon(
            modifier = Modifier.padding(horizontal = 16.dp),
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = ComposeAppTheme.colors.grey
        )
    }
}
