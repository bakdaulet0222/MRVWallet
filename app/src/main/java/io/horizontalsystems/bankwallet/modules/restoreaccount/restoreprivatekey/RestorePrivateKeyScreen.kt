package com.mrv.wallet.modules.restoreaccount.restoreprivatekey

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
import com.mrv.wallet.R
import com.mrv.wallet.core.stats.StatEntity
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.modules.restoreaccount.RestoreViewModel
import com.mrv.wallet.modules.restoreaccount.restoremenu.RestoreByMenu
import com.mrv.wallet.modules.restoreaccount.restoremenu.RestoreMenuViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.FormsInput
import com.mrv.wallet.ui.compose.components.FormsInputMultiline
import com.mrv.wallet.ui.compose.components.HeaderText
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.MenuItem

@Composable
fun RestorePrivateKey(
    restoreMenuViewModel: RestoreMenuViewModel,
    mainViewModel: RestoreViewModel,
    openSelectCoinsScreen: () -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel = viewModel<RestorePrivateKeyViewModel>(factory = RestorePrivateKeyModule.Factory())

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Restore_Advanced_Title),
                navigationIcon = {
                    HsBackButton(onClick = onBackClick)
                },
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Next),
                        onClick = {
                            viewModel.resolveAccountType()?.let { accountType ->
                                mainViewModel.setAccountData(accountType, viewModel.accountName, true, false, StatPage.ImportWalletFromKeyAdvanced)
                                openSelectCoinsScreen.invoke()

                                stat(
                                    page = StatPage.ImportWalletFromKeyAdvanced,
                                    event = StatEvent.Open(StatPage.RestoreSelect)
                                )
                            }
                        },
                        tint = ComposeAppTheme.colors.jacob
                    )
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            HeaderText(stringResource(id = R.string.ManageAccount_Name))
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                initial = viewModel.accountName,
                pasteEnabled = false,
                hint = viewModel.defaultName,
                onValueChange = viewModel::onEnterName
            )
            Spacer(Modifier.height(32.dp))

            RestoreByMenu(restoreMenuViewModel)

            Spacer(Modifier.height(32.dp))

            FormsInputMultiline(
                modifier = Modifier.padding(horizontal = 16.dp),
                hint = stringResource(id = R.string.Restore_PrivateKeyHint),
                state = viewModel.inputState,
                qrScannerEnabled = true,
                onValueChange = {
                    viewModel.onEnterPrivateKey(it)
                },
                onClear = {
                    stat(
                        page = StatPage.ImportWalletFromKeyAdvanced,
                        event = StatEvent.Clear(StatEntity.Key)
                    )
                },
                onPaste = {
                    stat(
                        page = StatPage.ImportWalletFromKeyAdvanced,
                        event = StatEvent.Paste(StatEntity.Key)
                    )
                },
                onScanQR = {
                    stat(
                        page = StatPage.ImportWalletFromKeyAdvanced,
                        event = StatEvent.ScanQr(StatEntity.Key)
                    )
                })

            Spacer(Modifier.height(32.dp))
        }
    }
}
