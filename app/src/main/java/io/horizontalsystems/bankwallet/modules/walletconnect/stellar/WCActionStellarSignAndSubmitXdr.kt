package com.mrv.wallet.modules.walletconnect.stellar

import com.google.gson.GsonBuilder
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.modules.multiswap.sendtransaction.SendTransactionData
import com.mrv.wallet.modules.multiswap.sendtransaction.SendTransactionServiceFactory
import com.mrv.wallet.modules.sendevmtransaction.SectionViewItem
import com.mrv.wallet.modules.sendevmtransaction.ViewItem
import com.mrv.wallet.modules.walletconnect.request.AbstractWCAction
import com.mrv.wallet.modules.walletconnect.request.WCActionState
import com.mrv.wallet.ui.compose.TranslatableString
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.stellarkit.StellarKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class WCActionStellarSignAndSubmitXdr(
    private val paramsJsonStr: String,
    private val peerName: String,
    private val stellarKit: StellarKit,
) : AbstractWCAction() {

    private val gson = GsonBuilder().create()
    private val params = gson.fromJson(paramsJsonStr, Params::class.java)
    private val xdr = params.xdr

    private val token = App.marketKit.token(TokenQuery(BlockchainType.Stellar, TokenType.Native))!!
    private val sendTransactionService = SendTransactionServiceFactory.create(token)
    private var sendTransactionState = sendTransactionService.stateFlow.value

    override fun start(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            sendTransactionService.stateFlow.collect { transactionState ->
                sendTransactionState = transactionState

                emitState()
            }
        }

        coroutineScope.launch {
            sendTransactionService.setSendTransactionData(
                SendTransactionData.Stellar.WithTransactionEnvelope(xdr)
            )
        }
    }

    override fun getTitle(): TranslatableString {
        return TranslatableString.ResString(R.string.WalletConnect_SendTransactionRequest_Title)
    }

    override fun getApproveButtonTitle(): TranslatableString {
        return TranslatableString.ResString(R.string.WalletConnect_SendTransactionRequest_ButtonSend)
    }

    override suspend fun performAction(): String {
        sendTransactionService.sendTransaction()

        return gson.toJson(mapOf("status" to "success"))
    }

    override fun createState(): WCActionState {
        val transaction = stellarKit.getTransaction(xdr)

        var sectionViewItems = WCStellarHelper.getTransactionViewItems(transaction, xdr, peerName)
        sendTransactionState.networkFee?.let { networkFee ->
            sectionViewItems += SectionViewItem(
                listOf(ViewItem.Fee(networkFee))
            )
        }

        return WCActionState(
            runnable = sendTransactionState.sendable,
            items = sectionViewItems
        )
    }

    data class Params(val xdr: String)
}

