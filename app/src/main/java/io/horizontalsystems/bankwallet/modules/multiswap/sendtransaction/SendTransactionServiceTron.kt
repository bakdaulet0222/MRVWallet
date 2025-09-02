package com.mrv.wallet.modules.multiswap.sendtransaction

import com.mrv.wallet.core.App
import com.mrv.wallet.core.ISendTronAdapter
import com.mrv.wallet.core.isNative
import com.mrv.wallet.entities.CoinValue
import com.mrv.wallet.modules.amount.AmountValidator
import com.mrv.wallet.modules.amount.SendAmountService
import com.mrv.wallet.modules.send.SendModule
import com.mrv.wallet.modules.send.tron.SendTronAddressService
import com.mrv.wallet.modules.send.tron.SendTronFeeService
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.math.RoundingMode

class SendTransactionServiceTron(token: Token) : AbstractSendTransactionService(false) {
    override val sendTransactionSettingsFlow = MutableStateFlow(SendTransactionSettings.Tron())
    private val adapter = App.adapterManager.getAdapterForToken<ISendTronAdapter>(token)!!
    private val feeToken = App.coinManager.getToken(TokenQuery(BlockchainType.Tron, TokenType.Native)) ?: throw IllegalArgumentException()

    private val amountService = SendAmountService(
        AmountValidator(),
        token.coin.code,
        adapter.balanceData.available.setScale(token.decimals, RoundingMode.DOWN),
        token.type.isNative,
    )
    private val addressService = SendTronAddressService(adapter, token)
    private val feeService = SendTronFeeService(adapter, feeToken)

    private var amountState = amountService.stateFlow.value
    private var addressState = addressService.stateFlow.value
    private var feeState = feeService.stateFlow.value

    private var networkFee: SendModule.AmountData? = null
    private var sendTransactionData: SendTransactionData.Tron? = null

    override fun start(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            amountService.stateFlow.collect {
                handleUpdatedAmountState(it)
            }
        }
        coroutineScope.launch {
            addressService.stateFlow.collect {
                handleUpdatedAddressState(it)
            }
        }
        coroutineScope.launch {
            feeService.stateFlow.collect {
                handleUpdatedFeeState(it)
            }
        }
    }

    private fun handleUpdatedFeeState(state: SendTronFeeService.State) {
        feeState = state

        networkFee = feeState.fee?.let {
            getAmountData(CoinValue(feeToken, it))
        }

        emitState()
    }

    private suspend fun handleUpdatedAddressState(state: SendTronAddressService.State) {
        addressState = state

        feeService.setTronAddress(addressState.tronAddress)

        emitState()
    }

    private suspend fun handleUpdatedAmountState(state: SendAmountService.State) {
        amountState = state

        feeService.setAmount(amountState.amount)

        emitState()
    }

    override suspend fun setSendTransactionData(data: SendTransactionData) {
        check(data is SendTransactionData.Tron)

        sendTransactionData = data

        if (data is SendTransactionData.Tron.WithContract) {
            feeService.setContract(data.contract)
        } else if (data is SendTransactionData.Tron.WithCreateTransaction) {
            feeService.setFeeLimit(data.transaction.raw_data.fee_limit)
        }

        emitState()

//        amountService.setAmount()

//        addressService.setAddress()
    }

    override suspend fun sendTransaction(mevProtectionEnabled: Boolean): SendTransactionResult {
        when (val tmpSendTransactionData = sendTransactionData) {
            is SendTransactionData.Tron.WithContract -> {
                adapter.send(tmpSendTransactionData.contract, feeState.feeLimit)
            }
            is SendTransactionData.Tron.WithCreateTransaction -> {
                adapter.send(tmpSendTransactionData.transaction)
            }
            null -> {
                adapter.send(amountState.amount!!, addressState.tronAddress!!, feeState.feeLimit)
            }
        }

        return SendTransactionResult.Tron
    }

    override fun createState() = SendTransactionServiceState(
        uuid = uuid,
        networkFee = networkFee,
        cautions = listOf(),
        sendable = sendTransactionData != null || (amountState.canBeSend && feeState.canBeSend && addressState.canBeSend),
        loading = false,
        fields = listOf(),
        extraFees = extraFees
    )
}
