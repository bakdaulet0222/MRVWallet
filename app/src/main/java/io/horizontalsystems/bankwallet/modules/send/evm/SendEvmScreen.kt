package com.mrv.wallet.modules.send.evm

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.core.slideFromBottomForResult
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.entities.Address
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.address.AddressParserModule
import com.mrv.wallet.modules.address.AddressParserViewModel
import com.mrv.wallet.modules.address.HSAddressCell
import com.mrv.wallet.modules.amount.AmountInputModeViewModel
import com.mrv.wallet.modules.amount.HSAmountInput
import com.mrv.wallet.modules.availablebalance.AvailableBalance
import com.mrv.wallet.modules.send.AddressRiskyBottomSheetAlert
import com.mrv.wallet.modules.send.SendScreen
import com.mrv.wallet.modules.send.evm.confirmation.SendEvmConfirmationFragment
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.ButtonPrimaryYellow
import com.mrv.wallet.ui.compose.components.VSpacer
import io.horizontalsystems.core.helpers.HudHelper
import io.horizontalsystems.marketkit.models.BlockchainType
import java.math.BigDecimal

@Composable
fun SendEvmScreen(
    title: String,
    navController: NavController,
    amountInputModeViewModel: AmountInputModeViewModel,
    address: Address,
    wallet: Wallet,
    amount: BigDecimal?,
    hideAddress: Boolean,
    riskyAddress: Boolean,
    sendEntryPointDestId: Int,
) {
    val viewModel = viewModel<SendEvmViewModel>(
        factory = SendEvmModule.Factory(wallet, address, hideAddress)
    )
    val uiState = viewModel.uiState

    val availableBalance = uiState.availableBalance
    val amountCaution = uiState.amountCaution
    val proceedEnabled = uiState.canBeSend
    val amountInputType = amountInputModeViewModel.inputType

    val paymentAddressViewModel = viewModel<AddressParserViewModel>(
        factory = AddressParserModule.Factory(wallet.token, amount)
    )
    val amountUnique = paymentAddressViewModel.amountUnique
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current

    ComposeAppTheme {
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        SendScreen(
            title = title,
            onBack = { navController.popBackStack() }
        ) {
            if (uiState.showAddressInput) {
                HSAddressCell(
                    title = stringResource(R.string.Send_Confirmation_To),
                    value = uiState.address.hex,
                    riskyAddress = riskyAddress,
                ) {
                    navController.popBackStack()
                }
                VSpacer(16.dp)
            }

            HSAmountInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                focusRequester = focusRequester,
                availableBalance = availableBalance,
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
                rate = viewModel.coinRate,
                amountUnique = amountUnique
            )

            VSpacer(8.dp)
            AvailableBalance(
                coinCode = wallet.coin.code,
                coinDecimal = viewModel.coinMaxAllowedDecimals,
                fiatDecimal = viewModel.fiatMaxAllowedDecimals,
                availableBalance = availableBalance,
                amountInputType = amountInputType,
                rate = viewModel.coinRate
            )

            ButtonPrimaryYellow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                title = stringResource(R.string.Button_Next),
                onClick = {
                    val sendData = viewModel.getSendData() ?: return@ButtonPrimaryYellow
                    if (!viewModel.hasConnection()) {
                        HudHelper.showErrorMessage(view, R.string.Hud_Text_NoInternet)
                    } else if (riskyAddress) {
                        keyboardController?.hide()
                        navController.slideFromBottomForResult<AddressRiskyBottomSheetAlert.Result>(
                            R.id.addressRiskyBottomSheetAlert,
                            AddressRiskyBottomSheetAlert.Input(
                                alertText = Translator.getString(R.string.Send_RiskyAddress_AlertText)
                            )
                        ) {
                            openSendConfirm(
                                sendData,
                                viewModel.wallet.token.blockchainType,
                                navController,
                                sendEntryPointDestId
                            )
                        }
                    } else {
                        openSendConfirm(
                            sendData,
                            viewModel.wallet.token.blockchainType,
                            navController,
                            sendEntryPointDestId
                        )
                    }
                },
                enabled = proceedEnabled
            )
        }
    }
}

private fun openSendConfirm(
    sendEvmData: SendEvmData,
    blockchainType: BlockchainType,
    navController: NavController,
    sendEntryPointDestId: Int
) {
    navController.slideFromRight(
        R.id.sendEvmConfirmationFragment,
        SendEvmConfirmationFragment.Input(
            sendData = sendEvmData,
            blockchainType = blockchainType,
            sendEntryPointDestId = sendEntryPointDestId
        )
    )
}
