package com.mrv.wallet.modules.watchaddress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.getInput
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEntity
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.modules.manageaccounts.ManageAccountsModule
import com.mrv.wallet.modules.watchaddress.selectblockchains.SelectBlockchainsFragment
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.FormsInput
import com.mrv.wallet.ui.compose.components.FormsInputMultiline
import com.mrv.wallet.ui.compose.components.HeaderText
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.InfoText
import com.mrv.wallet.ui.compose.components.MenuItem
import io.horizontalsystems.core.helpers.HudHelper
import kotlinx.coroutines.delay

class WatchAddressFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<ManageAccountsModule.Input>()
        val popUpToInclusiveId = input?.popOffOnSuccess ?: R.id.watchAddressFragment
        val inclusive = input?.popOffInclusive ?: true
        WatchAddressScreen(navController, popUpToInclusiveId, inclusive)
    }

}

@Composable
fun WatchAddressScreen(navController: NavController, popUpToInclusiveId: Int, inclusive: Boolean) {
    val view = LocalView.current

    val viewModel = viewModel<WatchAddressViewModel>(factory = WatchAddressModule.Factory())
    val uiState = viewModel.uiState
    val accountCreated = uiState.accountCreated
    val submitType = uiState.submitButtonType
    val accountType = uiState.accountType
    val accountName = uiState.accountName

    LaunchedEffect(accountCreated) {
        if (accountCreated) {
            HudHelper.showSuccessMessage(
                contenView = view,
                resId = R.string.Hud_Text_AddressAdded,
                icon = R.drawable.icon_binocule_24,
                iconTint = R.color.white
            )
            delay(300)
            navController.popBackStack(popUpToInclusiveId, inclusive)
        }
    }

    if (accountType != null) {
        viewModel.blockchainSelectionOpened()

        navController.slideFromRight(
            R.id.selectBlockchainsFragment,
            SelectBlockchainsFragment.Input(
                popUpToInclusiveId,
                inclusive,
                accountType,
                accountName
            )
        )
    }

    Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = stringResource(R.string.ManageAccounts_WatchAddress),
            navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack() })
            },
            menuItems = buildList {
                when (submitType) {
                    is SubmitButtonType.Watch -> {
                        add(
                            MenuItem(
                                title = TranslatableString.ResString(R.string.Button_Done),
                                onClick = viewModel::onClickWatch,
                                enabled = submitType.enabled,
                                tint = ComposeAppTheme.colors.jacob
                            )
                        )
                    }

                    is SubmitButtonType.Next -> {
                        add(
                            MenuItem(
                                title = TranslatableString.ResString(R.string.Button_Next),
                                onClick = viewModel::onClickNext,
                                enabled = submitType.enabled
                            )
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            HeaderText(stringResource(id = R.string.ManageAccount_Name))
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                initial = viewModel.accountName,
                pasteEnabled = false,
                hint = viewModel.defaultAccountName,
                onValueChange = viewModel::onEnterAccountName
            )
            Spacer(Modifier.height(32.dp))
            FormsInputMultiline(
                modifier = Modifier.padding(horizontal = 16.dp),
                hint = stringResource(id = R.string.Watch_Address_Hint),
                qrScannerEnabled = true,
                state = uiState.inputState,
                onValueChange = {
                    viewModel.onEnterInput(it)
                },
                onClear = {
                    stat(page = StatPage.WatchWallet, event = StatEvent.Clear(StatEntity.Key))
                },
                onScanQR = {
                    stat(page = StatPage.WatchWallet, event = StatEvent.ScanQr(StatEntity.Key))
                },
                onPaste = {
                    stat(page = StatPage.WatchWallet, event = StatEvent.Paste(StatEntity.Key))
                }
            )
            InfoText(
                text = stringResource(R.string.Watch_InfoText),
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
