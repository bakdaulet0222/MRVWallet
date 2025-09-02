package com.mrv.wallet.modules.balance.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.Caution
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.core.slideFromBottom
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.utils.ModuleField
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.backupalert.BackupAlert
import com.mrv.wallet.modules.balance.AccountViewItem
import com.mrv.wallet.modules.balance.BalanceModule
import com.mrv.wallet.modules.balance.BalanceViewModel
import com.mrv.wallet.modules.contacts.screen.ConfirmationBottomSheet
import com.mrv.wallet.modules.manageaccount.dialogs.BackupRequiredDialog
import com.mrv.wallet.modules.manageaccounts.ManageAccountsModule
import com.mrv.wallet.modules.qrscanner.QRScannerActivity
import com.mrv.wallet.modules.walletconnect.WCAccountTypeNotSupportedDialog
import com.mrv.wallet.modules.walletconnect.WCManager
import com.mrv.wallet.modules.walletconnect.list.WalletConnectListViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.MenuItem
import io.horizontalsystems.core.helpers.HudHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceForAccount(navController: NavController, accountViewItem: AccountViewItem) {
    val viewModel = viewModel<BalanceViewModel>(factory = BalanceModule.Factory())

    val context = LocalContext.current
    val modalBottomSheetState = androidx.compose.material3.rememberModalBottomSheetState()
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val qrScannerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.handleScannedData(
                    result.data?.getStringExtra(ModuleField.SCAN_ADDRESS) ?: ""
                )
            }
        }

    viewModel.uiState.errorMessage?.let { message ->
        val view = LocalView.current
        HudHelper.showErrorMessage(view, text = message)
        viewModel.errorShown()
    }

    when (viewModel.connectionResult) {
        WalletConnectListViewModel.ConnectionResult.Error -> {
            LaunchedEffect(viewModel.connectionResult) {
                coroutineScope.launch {
                    delay(300)
                    modalBottomSheetState.show()
                    isBottomSheetVisible = true
                }
            }
            viewModel.onHandleRoute()
        }

        else -> Unit
    }


    BackupAlert(navController)
    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = accountViewItem.name,
                menuItems = buildList {
                    if (!viewModel.uiState.balanceTabButtonsEnabled && !accountViewItem.isWatchAccount) {
                        add(
                            MenuItem(
                                title = TranslatableString.ResString(R.string.WalletConnect_NewConnect),
                                icon = R.drawable.ic_qr_scan_20,
                                onClick = {
                                    onScanClick(
                                        viewModel,
                                        qrScannerLauncher,
                                        context,
                                        navController
                                    )
                                }
                            )
                        )
                    }
                    add(
                        MenuItem(
                            title = TranslatableString.ResString(R.string.ManageAccounts_Title),
                            icon = R.drawable.ic_wallet_switch_24,
                            onClick = {
                                navController.slideFromBottom(
                                    R.id.manageAccountsFragment,
                                    ManageAccountsModule.Mode.Switcher
                                )

                                stat(
                                    page = StatPage.Balance,
                                    event = StatEvent.Open(StatPage.ManageWallets)
                                )
                            }
                        )
                    )
                }
            )
        }
    ) { paddingValues ->
        val uiState = viewModel.uiState

        Crossfade(
            targetState = uiState.viewState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            label = ""
        ) { viewState ->
            when (viewState) {
                ViewState.Success -> {
                    val balanceViewItems = uiState.balanceViewItems
                    BalanceItems(
                        balanceViewItems,
                        viewModel,
                        accountViewItem,
                        navController,
                        uiState,
                        viewModel.totalUiState,
                    ) {
                        onScanClick(viewModel, qrScannerLauncher, context, navController)
                    }
                }

                ViewState.Loading,
                is ViewState.Error,
                null -> {
                }
            }
        }
    }
    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                isBottomSheetVisible = false
            },
            sheetState = modalBottomSheetState,
            containerColor = ComposeAppTheme.colors.transparent
        ) {
            ConfirmationBottomSheet(
                title = stringResource(R.string.WalletConnect_Title),
                text = stringResource(R.string.WalletConnect_Error_InvalidUrl),
                iconPainter = painterResource(R.drawable.ic_wallet_connect_24),
                iconTint = ColorFilter.tint(ComposeAppTheme.colors.jacob),
                confirmText = stringResource(R.string.Button_TryAgain),
                cautionType = Caution.Type.Warning,
                cancelText = stringResource(R.string.Button_Cancel),
                onConfirm = {
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                        isBottomSheetVisible = false
                        qrScannerLauncher.launch(QRScannerActivity.getScanQrIntent(context, true))
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                        isBottomSheetVisible = false
                    }
                }
            )
        }
    }
}

private fun onScanClick(
    viewModel: BalanceViewModel,
    qrScannerLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    context: Context,
    navController: NavController
) {
    when (val state =
        viewModel.getWalletConnectSupportState()) {
        WCManager.SupportState.Supported -> {
            qrScannerLauncher.launch(
                QRScannerActivity.getScanQrIntent(context, true)
            )

            stat(
                page = StatPage.Balance,
                event = StatEvent.Open(StatPage.ScanQrCode)
            )
        }

        WCManager.SupportState.NotSupportedDueToNoActiveAccount -> {
            navController.slideFromBottom(R.id.wcErrorNoAccountFragment)
        }

        is WCManager.SupportState.NotSupportedDueToNonBackedUpAccount -> {
            val text =
                Translator.getString(R.string.WalletConnect_Error_NeedBackup)
            navController.slideFromBottom(
                R.id.backupRequiredDialog,
                BackupRequiredDialog.Input(state.account, text)
            )

            stat(
                page = StatPage.Balance,
                event = StatEvent.Open(StatPage.BackupRequired)
            )
        }

        is WCManager.SupportState.NotSupported -> {
            navController.slideFromBottom(
                R.id.wcAccountTypeNotSupportedDialog,
                WCAccountTypeNotSupportedDialog.Input(state.accountTypeDescription)
            )
        }
    }
}
