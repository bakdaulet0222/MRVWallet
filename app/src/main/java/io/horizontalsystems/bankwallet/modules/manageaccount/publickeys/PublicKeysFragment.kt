package com.mrv.wallet.modules.manageaccount.publickeys

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.entities.Account
import com.mrv.wallet.modules.manageaccount.evmaddress.EvmAddressFragment
import com.mrv.wallet.modules.manageaccount.showextendedkey.ShowExtendedKeyFragment
import com.mrv.wallet.modules.manageaccount.ui.KeyActionItem
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.HsBackButton

class PublicKeysFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Account>(navController) { account ->
            ManageAccountScreen(navController, account)
        }
    }

}

@Composable
fun ManageAccountScreen(navController: NavController, account: Account) {
    val viewModel = viewModel<PublicKeysViewModel>(factory = PublicKeysModule.Factory(account))

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.PublicKeys_Title),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))
            viewModel.viewState.evmAddress?.let { evmAddress ->
                KeyActionItem(
                    title = stringResource(id = R.string.PublicKeys_EvmAddress),
                    description = stringResource(R.string.PublicKeys_EvmAddress_Description)
                ) {
                    navController.slideFromRight(
                        R.id.evmAddressFragment,
                        EvmAddressFragment.Input(evmAddress)
                    )

                    stat(page = StatPage.PublicKeys, event = StatEvent.Open(StatPage.EvmAddress))
                }
            }
            viewModel.viewState.extendedPublicKey?.let { publicKey ->
                KeyActionItem(
                    title = stringResource(id = R.string.PublicKeys_AccountExtendedPublicKey),
                    description = stringResource(id = R.string.PublicKeys_AccountExtendedPublicKeyDescription),
                ) {
                    navController.slideFromRight(
                        R.id.showExtendedKeyFragment,
                        ShowExtendedKeyFragment.Input(
                            publicKey.hdKey,
                            publicKey.accountPublicKey
                        )
                    )

                    stat(
                        page = StatPage.PublicKeys,
                        event = StatEvent.Open(StatPage.AccountExtendedPublicKey)
                    )
                }
            }
        }
    }
}
