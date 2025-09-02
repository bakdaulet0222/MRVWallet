package com.mrv.wallet.modules.send.evm.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mrv.wallet.core.App
import com.mrv.wallet.core.ViewModelUiState
import com.mrv.wallet.core.ethereum.CautionViewItem
import com.mrv.wallet.core.ethereum.EvmCoinServiceFactory
import com.mrv.wallet.core.managers.RecentAddressManager
import com.mrv.wallet.entities.Address
import com.mrv.wallet.modules.multiswap.sendtransaction.SendTransactionData
import com.mrv.wallet.modules.multiswap.sendtransaction.SendTransactionServiceEvm
import com.mrv.wallet.modules.multiswap.ui.DataField
import com.mrv.wallet.modules.send.SendModule
import com.mrv.wallet.modules.send.evm.SendEvmData
import com.mrv.wallet.modules.sendevmtransaction.SectionViewItem
import com.mrv.wallet.modules.sendevmtransaction.SendEvmTransactionViewItemFactory
import io.horizontalsystems.erc20kit.decorations.OutgoingEip20Decoration
import io.horizontalsystems.ethereumkit.decorations.OutgoingDecoration
import io.horizontalsystems.ethereumkit.models.TransactionData
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.nftkit.decorations.OutgoingEip1155Decoration
import io.horizontalsystems.nftkit.decorations.OutgoingEip721Decoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendEvmConfirmationViewModel(
    private val sendEvmTransactionViewItemFactory: SendEvmTransactionViewItemFactory,
    val sendTransactionService: SendTransactionServiceEvm,
    private val transactionData: TransactionData,
    private val additionalInfo: SendEvmData.AdditionalInfo?,
    private val recentAddressManager: RecentAddressManager,
    private val blockchainType: BlockchainType
) : ViewModelUiState<SendEvmConfirmationUiState>() {
    private var sendTransactionState = sendTransactionService.stateFlow.value

    private val transactionDecoration = sendTransactionService.decorate(transactionData)
    private val sectionViewItems = sendEvmTransactionViewItemFactory.getItems(
        transactionData,
        additionalInfo,
        transactionDecoration
    )

    init {
        viewModelScope.launch {
            sendTransactionService.stateFlow.collect { transactionState ->
                sendTransactionState = transactionState
                emitState()
            }
        }

        sendTransactionService.start(viewModelScope)

        viewModelScope.launch {
            sendTransactionService.setSendTransactionData(SendTransactionData.Evm(transactionData, null))
        }
    }

    override fun createState() = SendEvmConfirmationUiState(
        networkFee = sendTransactionState.networkFee,
        cautions = sendTransactionState.cautions,
        sendEnabled = sendTransactionState.sendable,
        transactionFields = sendTransactionState.fields,
        sectionViewItems = sectionViewItems
    )

    suspend fun send() = withContext(Dispatchers.Default) {
        sendTransactionService.sendTransaction()

        val address = when (transactionDecoration) {
            is OutgoingEip20Decoration -> {
                transactionDecoration.to.eip55
            }

            is OutgoingEip721Decoration -> {
                transactionDecoration.to.eip55
            }

            is OutgoingEip1155Decoration -> {
                transactionDecoration.to.eip55
            }

            is OutgoingDecoration -> {
                transactionDecoration.to.eip55
            }

            else -> null
        }
        address?.let {
            recentAddressManager.setRecentAddress(Address(address), blockchainType)
        }
    }

    class Factory(
        private val transactionData: TransactionData,
        private val additionalInfo: SendEvmData.AdditionalInfo?,
        private val blockchainType: BlockchainType
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val sendTransactionService = SendTransactionServiceEvm(blockchainType)
            val feeToken = App.evmBlockchainManager.getBaseToken(blockchainType)!!
            val coinServiceFactory = EvmCoinServiceFactory(
                feeToken,
                App.marketKit,
                App.currencyManager,
                App.coinManager
            )

            val sendEvmTransactionViewItemFactory = SendEvmTransactionViewItemFactory(
                App.evmLabelManager,
                coinServiceFactory,
                App.contactsRepository,
                blockchainType
            )

            return SendEvmConfirmationViewModel(
                sendEvmTransactionViewItemFactory,
                sendTransactionService,
                transactionData,
                additionalInfo,
                App.recentAddressManager,
                blockchainType
            ) as T
        }
    }

}

data class SendEvmConfirmationUiState(
    val networkFee: SendModule.AmountData?,
    val cautions: List<CautionViewItem>,
    val sendEnabled: Boolean,
    val transactionFields: List<DataField>,
    val sectionViewItems: List<SectionViewItem>
)