package com.mrv.wallet.modules.eip20revoke

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mrv.wallet.core.App
import com.mrv.wallet.core.IAdapterManager
import com.mrv.wallet.core.IWalletManager
import com.mrv.wallet.core.ViewModelUiState
import com.mrv.wallet.core.adapters.Eip20Adapter
import com.mrv.wallet.core.adapters.Trc20Adapter
import com.mrv.wallet.core.ethereum.CautionViewItem
import com.mrv.wallet.core.isEvm
import com.mrv.wallet.core.managers.CurrencyManager
import com.mrv.wallet.entities.Currency
import com.mrv.wallet.modules.contacts.ContactsRepository
import com.mrv.wallet.modules.contacts.model.Contact
import com.mrv.wallet.modules.multiswap.FiatService
import com.mrv.wallet.modules.multiswap.sendtransaction.AbstractSendTransactionService
import com.mrv.wallet.modules.multiswap.sendtransaction.SendTransactionData
import com.mrv.wallet.modules.multiswap.sendtransaction.SendTransactionServiceFactory
import com.mrv.wallet.modules.send.SendModule
import io.horizontalsystems.ethereumkit.models.Address
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID

class Eip20RevokeConfirmViewModel(
    private val token: Token,
    private val allowance: BigDecimal,
    private val spenderAddress: String,
    private val walletManager: IWalletManager,
    private val adapterManager: IAdapterManager,
    val sendTransactionService: AbstractSendTransactionService,
    private val currencyManager: CurrencyManager,
    private val fiatService: FiatService,
    private val contactsRepository: ContactsRepository,
) : ViewModelUiState<Eip20RevokeUiState>() {
    private val currency = currencyManager.baseCurrency
    private var sendTransactionState = sendTransactionService.stateFlow.value
    private var fiatAmount: BigDecimal? = null
    private val contact = contactsRepository.getContactsFiltered(
        blockchainType = token.blockchainType,
        addressQuery = spenderAddress
    ).firstOrNull()

    override fun createState() = Eip20RevokeUiState(
        token = token,
        allowance = allowance,
        networkFee = sendTransactionState.networkFee,
        cautions = sendTransactionState.cautions,
        currency = currency,
        fiatAmount = fiatAmount,
        spenderAddress = spenderAddress,
        contact = contact,
        revokeEnabled = sendTransactionState.sendable
    )

    val uuid = UUID.randomUUID().toString()

    init {
        fiatService.setCurrency(currency)
        fiatService.setToken(token)
        fiatService.setAmount(allowance)

        viewModelScope.launch {
            fiatService.stateFlow.collect {
                fiatAmount = it.fiatAmount
                emitState()
            }
        }

        viewModelScope.launch {
            sendTransactionService.stateFlow.collect { transactionState ->
                sendTransactionState = transactionState
                emitState()
            }
        }

        sendTransactionService.start(viewModelScope)

        when {
            token.blockchainType.isEvm -> prepareEvmRevokeTransaction()
            token.blockchainType == BlockchainType.Tron -> prepareTronRevokeTransaction()
            else -> throw IllegalArgumentException("Unsupported blockchain type for EIP-20 revoke")
        }
    }

    private fun prepareTronRevokeTransaction() {
        val trc20Adapter = adapterManager.getAdapterForToken<Trc20Adapter>(token)
            ?: throw IllegalStateException("Trc20Adapter not found for token")
        viewModelScope.launch {
            val triggerSmartContract =
                trc20Adapter.approveTrc20TriggerSmartContract(spenderAddress, BigDecimal.ZERO)
            sendTransactionService.setSendTransactionData(
                SendTransactionData.Tron.WithContract(triggerSmartContract)
            )
        }
    }

    private fun prepareEvmRevokeTransaction() {
        val eip20Adapter =
            walletManager.activeWallets.firstOrNull { it.token == token }?.let { wallet ->
                adapterManager.getAdapterForWallet<Eip20Adapter>(wallet)
            } ?: throw IllegalStateException("Eip20Adapter not found for token")
        viewModelScope.launch {
            val transactionData =
                eip20Adapter.buildRevokeTransactionData(Address(spenderAddress))
            sendTransactionService.setSendTransactionData(
                SendTransactionData.Evm(transactionData, null)
            )
        }
    }

    suspend fun revoke() = withContext(Dispatchers.Default) {
        sendTransactionService.sendTransaction()
    }

    class Factory(
        private val token: Token,
        private val spenderAddress: String,
        private val allowance: BigDecimal,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val sendTransactionService = SendTransactionServiceFactory.create(token)//SendTransactionServiceEvm(token.blockchainType)

            return Eip20RevokeConfirmViewModel(
                token,
                allowance,
                spenderAddress,
                App.walletManager,
                App.adapterManager,
                sendTransactionService,
                App.currencyManager,
                FiatService(App.marketKit),
                App.contactsRepository
            ) as T
        }
    }
}

data class Eip20RevokeUiState(
    val token: Token,
    val allowance: BigDecimal,
    val networkFee: SendModule.AmountData?,
    val cautions: List<CautionViewItem>,
    val currency: Currency,
    val fiatAmount: BigDecimal?,
    val spenderAddress: String,
    val contact: Contact?,
    val revokeEnabled: Boolean,
)
