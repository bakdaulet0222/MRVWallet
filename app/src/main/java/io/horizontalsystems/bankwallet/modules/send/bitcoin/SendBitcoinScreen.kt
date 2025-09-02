package com.mrv.wallet.modules.send.bitcoin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mrv.wallet.R
import com.mrv.wallet.core.composablePage
import com.mrv.wallet.core.composablePopup
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.core.slideFromBottomForResult
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.modules.address.AddressParserModule
import com.mrv.wallet.modules.address.AddressParserViewModel
import com.mrv.wallet.modules.address.HSAddressCell
import com.mrv.wallet.modules.amount.AmountInputModeViewModel
import com.mrv.wallet.modules.amount.HSAmountInput
import com.mrv.wallet.modules.availablebalance.AvailableBalance
import com.mrv.wallet.modules.fee.HSFeeRaw
import com.mrv.wallet.modules.memo.HSMemoInput
import com.mrv.wallet.modules.send.AddressRiskyBottomSheetAlert
import com.mrv.wallet.modules.send.SendConfirmationFragment
import com.mrv.wallet.modules.send.bitcoin.advanced.BtcTransactionInputSortInfoScreen
import com.mrv.wallet.modules.send.bitcoin.advanced.FeeRateCaution
import com.mrv.wallet.modules.send.bitcoin.advanced.SendBtcAdvancedSettingsScreen
import com.mrv.wallet.modules.send.bitcoin.utxoexpert.UtxoExpertModeScreen
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.ButtonPrimaryYellow
import com.mrv.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.RowUniversal
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.subhead2_grey
import com.mrv.wallet.ui.compose.components.subhead2_leah
import java.math.BigDecimal


const val SendBtcPage = "send_btc"
const val SendBtcAdvancedSettingsPage = "send_btc_advanced_settings"
const val TransactionInputsSortInfoPage = "transaction_input_sort_info_settings"
const val UtxoExpertModePage = "utxo_expert_mode_page"

@Composable
fun SendBitcoinNavHost(
    title: String,
    fragmentNavController: NavController,
    viewModel: SendBitcoinViewModel,
    amountInputModeViewModel: AmountInputModeViewModel,
    sendEntryPointDestId: Int,
    amount: BigDecimal?,
    riskyAddress: Boolean
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SendBtcPage,
    ) {
        composable(SendBtcPage) {
            SendBitcoinScreen(
                title,
                fragmentNavController,
                navController,
                viewModel,
                amountInputModeViewModel,
                sendEntryPointDestId,
                amount,
                riskyAddress
            )
        }
        composablePage(SendBtcAdvancedSettingsPage) {
            SendBtcAdvancedSettingsScreen(
                fragmentNavController = fragmentNavController,
                navController = navController,
                sendBitcoinViewModel = viewModel,
                amountInputType = amountInputModeViewModel.inputType,
            )
        }
        composablePopup(TransactionInputsSortInfoPage) { BtcTransactionInputSortInfoScreen { navController.popBackStack() } }
        composablePage(UtxoExpertModePage) {
            UtxoExpertModeScreen(
                adapter = viewModel.adapter,
                token = viewModel.wallet.token,
                customUnspentOutputs = viewModel.customUnspentOutputs,
                updateUnspentOutputs = {
                    viewModel.updateCustomUnspentOutputs(it)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun SendBitcoinScreen(
    title: String,
    fragmentNavController: NavController,
    composeNavController: NavController,
    viewModel: SendBitcoinViewModel,
    amountInputModeViewModel: AmountInputModeViewModel,
    sendEntryPointDestId: Int,
    amount: BigDecimal?,
    riskyAddress: Boolean,
) {
    val wallet = viewModel.wallet
    val uiState = viewModel.uiState

    val availableBalance = uiState.availableBalance
    val amountCaution = uiState.amountCaution
    val fee = uiState.fee
    val proceedEnabled = uiState.canBeSend
    val amountInputType = amountInputModeViewModel.inputType
    val feeRateCaution = uiState.feeRateCaution
    val keyboardController = LocalSoftwareKeyboardController.current

    val rate = viewModel.coinRate

    val paymentAddressViewModel = viewModel<AddressParserViewModel>(
        factory = AddressParserModule.Factory(wallet.token, amount)
    )
    val amountUnique = paymentAddressViewModel.amountUnique

    ComposeAppTheme {
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
            AppBar(
                title = title,
                navigationIcon = {
                    HsBackButton(onClick = { fragmentNavController.popBackStack() })
                },
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.SendEvmSettings_Title),
                        icon = R.drawable.ic_manage_2,
                        onClick = { composeNavController.navigate(SendBtcAdvancedSettingsPage) }
                    ),
                )
            )

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (uiState.showAddressInput) {
                    HSAddressCell(
                        title = stringResource(R.string.Send_Confirmation_To),
                        value = uiState.address.hex,
                        riskyAddress = riskyAddress
                    ) {
                        fragmentNavController.popBackStack()
                    }
                    VSpacer(16.dp)
                }

                HSAmountInput(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    focusRequester = focusRequester,
                    availableBalance = availableBalance ?: BigDecimal.ZERO,
                    caution = amountCaution,
                    coinCode = wallet.coin.code,
                    coinDecimal = viewModel.coinMaxAllowedDecimals,
                    fiatDecimal = viewModel.fiatMaxAllowedDecimals,
                    onClickHint = {
                        amountInputModeViewModel.onToggleInputType()
                    },
                    onValueChange = {
                        viewModel.onEnterAmount(it)
                    },
                    inputType = amountInputType,
                    rate = rate,
                    amountUnique = amountUnique
                )

                VSpacer(8.dp)
                AvailableBalance(
                    coinCode = wallet.coin.code,
                    coinDecimal = viewModel.coinMaxAllowedDecimals,
                    fiatDecimal = viewModel.fiatMaxAllowedDecimals,
                    availableBalance = availableBalance,
                    amountInputType = amountInputType,
                    rate = rate
                )

                VSpacer(16.dp)
                HSMemoInput(maxLength = 120) {
                    viewModel.onEnterMemo(it)
                }

                VSpacer(16.dp)
                CellUniversalLawrenceSection(
                    buildList {
                        uiState.utxoData?.let { utxoData ->
                            add {
                                UtxoCell(
                                    utxoData = utxoData,
                                    onClick = {
                                        composeNavController.navigate(UtxoExpertModePage)
                                    }
                                )
                            }
                        }
                        add {
                            HSFeeRaw(
                                coinCode = wallet.coin.code,
                                coinDecimal = viewModel.coinMaxAllowedDecimals,
                                fee = fee,
                                amountInputType = amountInputType,
                                rate = rate,
                                navController = fragmentNavController
                            )
                        }
                    }
                )

                feeRateCaution?.let {
                    FeeRateCaution(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        feeRateCaution = feeRateCaution
                    )
                }

                ButtonPrimaryYellow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    title = stringResource(R.string.Button_Next),
                    onClick = {
                        if (riskyAddress) {
                            keyboardController?.hide()
                            fragmentNavController.slideFromBottomForResult<AddressRiskyBottomSheetAlert.Result>(
                                R.id.addressRiskyBottomSheetAlert,
                                AddressRiskyBottomSheetAlert.Input(
                                    alertText = Translator.getString(R.string.Send_RiskyAddress_AlertText)
                                )
                            ) {
                                openConfirm(fragmentNavController, sendEntryPointDestId)
                            }
                        } else {
                            openConfirm(fragmentNavController, sendEntryPointDestId)
                        }
                    },
                    enabled = proceedEnabled
                )
            }
        }
    }
}

private fun openConfirm(
    fragmentNavController: NavController,
    sendEntryPointDestId: Int
) {
    fragmentNavController.slideFromRight(
        R.id.sendConfirmation,
        SendConfirmationFragment.Input(
            SendConfirmationFragment.Type.Bitcoin,
            sendEntryPointDestId
        )
    )
}

@Composable
fun UtxoCell(
    utxoData: SendBitcoinModule.UtxoData,
    onClick: () -> Unit
) {
    RowUniversal(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        onClick = onClick
    ) {
        subhead2_grey(
            text = stringResource(R.string.Send_Utxos),
            modifier = Modifier.weight(1f)
        )
        subhead2_leah(text = utxoData.value)
        HSpacer(8.dp)
        when (utxoData.type) {
            SendBitcoinModule.UtxoType.Auto -> {
                Icon(
                    painter = painterResource(R.drawable.ic_edit_20),
                    contentDescription = null,
                    tint = ComposeAppTheme.colors.grey
                )
            }

            SendBitcoinModule.UtxoType.Manual -> {
                Icon(
                    painter = painterResource(R.drawable.ic_edit_20),
                    contentDescription = null,
                    tint = ComposeAppTheme.colors.jacob
                )
            }

            null -> {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = ComposeAppTheme.colors.grey
                )
            }
        }
    }
}
