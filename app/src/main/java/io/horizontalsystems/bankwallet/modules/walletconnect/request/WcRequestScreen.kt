package com.mrv.wallet.modules.walletconnect.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.web3.wallet.client.Wallet
import com.mrv.wallet.R
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.modules.evmfee.ButtonsGroupWithShade
import com.mrv.wallet.modules.sendevmtransaction.SectionView
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.ButtonPrimaryDefault
import com.mrv.wallet.ui.compose.components.ButtonPrimaryYellow
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.VSpacer

@Composable
fun WcRequestScreen(
    navController: NavController,
    sessionRequest: Wallet.Model.SessionRequest,
    wcAction: AbstractWCAction
) {
    val viewModel = viewModel<WCRequestViewModel>(
        factory = WCRequestViewModel.Factory(sessionRequest, wcAction)
    )

    val uiState = viewModel.uiState

    LaunchedEffect(uiState.finish) {
        if (uiState.finish) {
            navController.popBackStack()
        }
    }

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = uiState.title.getString(),
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Close),
                        icon = R.drawable.ic_close,
                        onClick = { navController.popBackStack() }
                    )
                )
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    ButtonPrimaryYellow(
                        modifier = Modifier.fillMaxWidth(),
                        title = uiState.approveButtonTitle.getString(),
                        onClick = viewModel::approve,
                        enabled = uiState.runnable
                    )
                    VSpacer(16.dp)
                    ButtonPrimaryDefault(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.Button_Reject),
                        onClick = viewModel::reject
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .fillMaxWidth()
        ) {
            VSpacer(12.dp)

            uiState.contentItems.forEach { sectionViewItem ->
                SectionView(
                    sectionViewItem.viewItems,
                    navController,
                    StatPage.WalletConnect
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
