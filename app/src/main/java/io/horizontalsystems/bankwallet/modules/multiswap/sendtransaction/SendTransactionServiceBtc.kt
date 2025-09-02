package com.mrv.wallet.modules.multiswap.sendtransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.HSCaution
import com.mrv.wallet.core.ISendBitcoinAdapter
import com.mrv.wallet.core.adapters.BitcoinFeeInfo
import com.mrv.wallet.core.factories.FeeRateProviderFactory
import com.mrv.wallet.entities.Address
import com.mrv.wallet.entities.CoinValue
import com.mrv.wallet.modules.amount.AmountInputType
import com.mrv.wallet.modules.amount.AmountValidator
import com.mrv.wallet.modules.evmfee.EvmSettingsInput
import com.mrv.wallet.modules.fee.HSFeeRaw
import com.mrv.wallet.modules.multiswap.ui.DataField
import com.mrv.wallet.modules.send.SendModule
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinAddressService
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinAmountService
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinFeeRateService
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinFeeService
import com.mrv.wallet.modules.send.bitcoin.advanced.FeeRateCaution
import com.mrv.wallet.modules.send.bitcoin.settings.SendBtcSettingsViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.mrv.wallet.ui.compose.components.HsIconButton
import com.mrv.wallet.ui.compose.components.InfoText
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.VSpacer
import io.horizontalsystems.bitcoincore.storage.UtxoFilters
import io.horizontalsystems.marketkit.models.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class SendTransactionServiceBtc(private val token: Token) : AbstractSendTransactionService(true) {
    private val adapter = App.adapterManager.getAdapterForToken<ISendBitcoinAdapter>(token)!!
    private val provider = FeeRateProviderFactory.provider(token.blockchainType)!!
    private val feeService = SendBitcoinFeeService(adapter)
    private val feeRateService = SendBitcoinFeeRateService(provider)
    private val amountService = SendBitcoinAmountService(adapter, token.coin.code, AmountValidator())
    private val addressService = SendBitcoinAddressService(adapter)

    private var feeRateState = feeRateService.stateFlow.value
    private var bitcoinFeeInfo = feeService.bitcoinFeeInfoFlow.value
    private var amountState = amountService.stateFlow.value
    private var addressState = addressService.stateFlow.value

    private var memo: String? = null
    private var dustThreshold: Int? = null
    private var changeToFirstInput: Boolean = false
    private var utxoFilters: UtxoFilters = UtxoFilters()
    private var networkFee: SendModule.AmountData? = null

    private var fields = listOf<DataField>()

    override val sendTransactionSettingsFlow = MutableStateFlow(SendTransactionSettings.Btc())

    override fun start(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            feeRateService.stateFlow.collect {
                handleFeeRateState(it)
            }
        }
        coroutineScope.launch {
            feeService.bitcoinFeeInfoFlow.collect {
                handleBitcoinFeeInfo(it)
            }
        }
        coroutineScope.launch {
            amountService.stateFlow.collect {
                handleAmountState(it)
            }
        }
        coroutineScope.launch {
            addressService.stateFlow.collect {
                handleAddressState(it)
            }
        }

        coroutineScope.launch {
            feeRateService.start()
        }
    }

    private fun handleAddressState(state: SendBitcoinAddressService.State) {
        addressState = state

        amountService.setValidAddress(addressState.validAddress)
        feeService.setValidAddress(addressState.validAddress)

        emitState()
    }

    private fun handleAmountState(state: SendBitcoinAmountService.State) {
        amountState = state

        feeService.setAmount(amountState.amount)

        emitState()
    }

    private fun handleBitcoinFeeInfo(info: BitcoinFeeInfo?) {
        bitcoinFeeInfo = info

        refreshNetworkFee()

        emitState()
    }

    private fun refreshNetworkFee() {
        networkFee = bitcoinFeeInfo?.fee?.let { fee ->
            getAmountData(CoinValue(token, fee))
        }
    }

    private fun handleFeeRateState(state: SendBitcoinFeeRateService.State) {
        feeRateState = state

        feeService.setFeeRate(feeRateState.feeRate)
        amountService.setFeeRate(feeRateState.feeRate)

        emitState()
    }

    override fun createState() = SendTransactionServiceState(
        uuid = uuid,
        networkFee = networkFee,
        cautions = listOfNotNull(amountState.amountCaution, feeRateState.feeRateCaution).map(HSCaution::toCautionViewItem),
        sendable = amountState.canBeSend && feeRateState.canBeSend && addressState.canBeSend,
        loading = false,
        fields = fields,
        extraFees = extraFees
    )

    override suspend fun setSendTransactionData(data: SendTransactionData) {
        check(data is SendTransactionData.Btc)

        memo = data.memo
        dustThreshold = data.dustThreshold
        changeToFirstInput = data.changeToFirstInput
        utxoFilters = data.utxoFilters

        feeRateService.setRecommendedAndMin(data.recommendedGasRate, data.recommendedGasRate)

        feeService.setMemo(memo)
        feeService.setDustThreshold(dustThreshold)
        feeService.setChangeToFirstInput(changeToFirstInput)
        feeService.setUtxoFilters(utxoFilters)

        amountService.setMemo(memo)
        amountService.setDustThreshold(dustThreshold)
        amountService.setChangeToFirstInput(changeToFirstInput)
        amountService.setUtxoFilters(utxoFilters)
        amountService.setAmount(data.amount)

        addressService.setAddress(Address(data.address))

        setExtraFeesMap(data.feesMap)
    }

    @Composable
    override fun GetSettingsContent(navController: NavController) {
        val sendSettingsViewModel = viewModel<SendBtcSettingsViewModel>(
            factory = SendBtcSettingsViewModel.Factory(feeRateService, feeService, token)
        )

        SendBtcFeeSettingsScreen(navController, sendSettingsViewModel)
    }

    override suspend fun sendTransaction(mevProtectionEnabled: Boolean): SendTransactionResult.Btc {
        val transactionRecord = adapter.send(
            amount = amountState.amount!!,
            address = addressState.validAddress?.hex!!,
            memo = memo,
            feeRate = feeRateState.feeRate!!,
            unspentOutputs = null,
            pluginData = null,
            transactionSorting = null,
            rbfEnabled = false,
            dustThreshold = dustThreshold,
            changeToFirstInput = changeToFirstInput,
            utxoFilters = utxoFilters
        )

        return SendTransactionResult.Btc(transactionRecord)
    }
}

@Composable
fun SendBtcFeeSettingsScreen(
    navController: NavController,
    viewModel: SendBtcSettingsViewModel
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .background(color = ComposeAppTheme.colors.tyler)
    ) {
        AppBar(
            title = stringResource(R.string.SendEvmSettings_Title),
            navigationIcon = {
                HsIconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left_24),
                        contentDescription = "back button",
                        tint = ComposeAppTheme.colors.jacob
                    )
                }
            },
            menuItems = listOf(
                MenuItem(
                    title = TranslatableString.ResString(R.string.Button_Reset),
                    enabled = uiState.resetEnabled,
                    tint = ComposeAppTheme.colors.jacob,
                    onClick = {
                        viewModel.reset()
                    }
                )
            )
        )

        VSpacer(12.dp)
        CellUniversalLawrenceSection(
            listOf {
                HSFeeRaw(
                    coinCode = viewModel.token.coin.code,
                    coinDecimal = viewModel.coinMaxAllowedDecimals,
                    fee = uiState.fee,
                    amountInputType = AmountInputType.COIN,
                    rate = uiState.rate,
                    navController = navController
                )
            }
        )

        if (viewModel.feeRateChangeable) {
            VSpacer(24.dp)
            EvmSettingsInput(
                title = stringResource(R.string.FeeSettings_FeeRate),
                info = stringResource(R.string.FeeSettings_FeeRate_Info),
                value = uiState.feeRate?.toBigDecimal() ?: BigDecimal.ZERO,
                decimals = 0,
                caution = uiState.feeRateCaution,
                navController = navController,
                onValueChange = {
                    viewModel.updateFeeRate(it.toInt())
                },
                onClickIncrement = {
                    viewModel.incrementFeeRate()
                },
                onClickDecrement = {
                    viewModel.decrementFeeRate()
                }
            )
            InfoText(
                text = stringResource(R.string.FeeSettings_FeeRate_RecommendedInfo),
            )
        }

        uiState.feeRateCaution?.let {
            FeeRateCaution(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp
                ),
                feeRateCaution = it
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

}