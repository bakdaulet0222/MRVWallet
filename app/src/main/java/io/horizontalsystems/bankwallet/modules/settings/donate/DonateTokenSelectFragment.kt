package com.mrv.wallet.modules.settings.donate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.entities.Address
import com.mrv.wallet.modules.send.SendFragment
import com.mrv.wallet.modules.tokenselect.TokenSelectScreen
import com.mrv.wallet.modules.tokenselect.TokenSelectViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.mrv.wallet.ui.compose.components.RowUniversal
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.body_leah

class DonateTokenSelectFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        TokenSelectScreen(
            navController = navController,
            title = stringResource(R.string.Settings_Donate),
            onClickItem = { viewItem ->
                val donateAddress: String? =
                    App.appConfigProvider.donateAddresses[viewItem.wallet.token.blockchainType]
                donateAddress?.let {
                    val sendTitle = Translator.getString(
                        R.string.Settings_DonateToken,
                        viewItem.wallet.token.fullCoin.coin.code
                    )
                    navController.slideFromRight(
                        R.id.sendXFragment,
                        SendFragment.Input(
                            wallet = viewItem.wallet,
                            title = sendTitle,
                            sendEntryPointDestId = R.id.sendTokenSelectFragment,
                            address = Address(donateAddress),
                            hideAddress = true
                        )
                    )

                    stat(page = StatPage.Donate, event = StatEvent.OpenSend(viewItem.wallet.token))
                }

            },
            viewModel = viewModel(factory = TokenSelectViewModel.FactoryForSend()),
            emptyItemsText = stringResource(R.string.Balance_NoAssetsToSend)
        ) {
            DonateHeader(
                onClick = {
                    navController.slideFromRight(R.id.donateAddressesFragment)

                    stat(page = StatPage.Donate, event = StatEvent.Open(StatPage.DonateAddressList))
                }
            )
        }
    }
}

@Composable
private fun DonateHeader(onClick: () -> Unit) {
    VSpacer(12.dp)
    CellUniversalLawrenceSection(
        listOf({
            RowUniversal(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = onClick
            ) {
                body_leah(
                    text = stringResource(R.string.Settings_Donate_DonationAddresses),
                    maxLines = 1,
                )
                Spacer(Modifier.weight(1f))
                Image(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                )
            }
        })
    )
    VSpacer(32.dp)
}

@Preview
@Composable
private fun DonateHeaderPreview() {
    ComposeAppTheme {
        DonateHeader({})
    }
}