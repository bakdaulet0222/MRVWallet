package com.mrv.wallet.modules.transactionInfo

import com.mrv.wallet.core.ITransactionsAdapter
import com.mrv.wallet.core.adapters.StellarTransactionRecord
import com.mrv.wallet.core.adapters.TonTransactionRecord
import com.mrv.wallet.core.managers.CurrencyManager
import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.entities.CurrencyValue
import com.mrv.wallet.entities.nft.NftAssetBriefMetadata
import com.mrv.wallet.entities.nft.NftUid
import com.mrv.wallet.entities.transactionrecords.TransactionRecord
import com.mrv.wallet.entities.transactionrecords.bitcoin.BitcoinIncomingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.bitcoin.BitcoinOutgoingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.ApproveTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.ContractCallTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.EvmIncomingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.EvmOutgoingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.EvmTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.ExternalContractCallTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.SwapTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.UnknownSwapTransactionRecord
import com.mrv.wallet.entities.transactionrecords.nftUids
import com.mrv.wallet.entities.transactionrecords.solana.SolanaIncomingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.solana.SolanaOutgoingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.solana.SolanaUnknownTransactionRecord
import com.mrv.wallet.entities.transactionrecords.tron.TronApproveTransactionRecord
import com.mrv.wallet.entities.transactionrecords.tron.TronContractCallTransactionRecord
import com.mrv.wallet.entities.transactionrecords.tron.TronExternalContractCallTransactionRecord
import com.mrv.wallet.entities.transactionrecords.tron.TronIncomingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.tron.TronOutgoingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.tron.TronTransactionRecord
import com.mrv.wallet.entities.transactionrecords.zcash.ZcashShieldingTransactionRecord
import com.mrv.wallet.modules.transactions.FilterTransactionType
import com.mrv.wallet.modules.transactions.NftMetadataService
import com.mrv.wallet.modules.transactions.TransactionSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class TransactionInfoService(
    val transactionRecord: TransactionRecord,
    private val adapter: ITransactionsAdapter,
    private val marketKit: MarketKitWrapper,
    private val currencyManager: CurrencyManager,
    private val nftMetadataService: NftMetadataService,
    balanceHidden: Boolean,
) {

    val transactionHash: String get() = transactionRecord.transactionHash
    val source: TransactionSource get() = transactionRecord.source

    private val _transactionInfoItemFlow = MutableStateFlow<TransactionInfoItem?>(null)
    val transactionInfoItemFlow = _transactionInfoItemFlow.filterNotNull()

    var transactionInfoItem = TransactionInfoItem(
        transactionRecord,
        adapter.lastBlockInfo,
        TransactionInfoModule.ExplorerData(adapter.explorerTitle, adapter.getTransactionUrl(transactionRecord.transactionHash)),
        mapOf(),
        mapOf(),
        balanceHidden
    )
        private set(value) {
            field = value
            _transactionInfoItemFlow.update { value }
        }

    private val coinUidsForRates: List<String>
        get() {
            val coinUids = mutableListOf<String?>()

            val txCoinTypes = when (val tx = transactionRecord) {
                is StellarTransactionRecord -> listOf(tx.mainValue?.coinUid, tx.fee?.coinUid)
                is TonTransactionRecord -> buildList {
                    add(tx.mainValue?.coinUid)
                    add(tx.fee.coinUid)

                    tx.actions.forEach { action ->
                        val actionType = action.type
                        when (actionType) {
                            is TonTransactionRecord.Action.Type.Burn -> {
                                add(actionType.value.coinUid)
                            }
                            is TonTransactionRecord.Action.Type.ContractCall -> {
                                add(actionType.value.coinUid)
                            }
                            is TonTransactionRecord.Action.Type.Mint -> {
                                add(actionType.value.coinUid)
                            }
                            is TonTransactionRecord.Action.Type.Receive -> {
                                add(actionType.value.coinUid)
                            }
                            is TonTransactionRecord.Action.Type.Send -> {
                                add(actionType.value.coinUid)
                            }
                            is TonTransactionRecord.Action.Type.Swap -> {
                                add(actionType.valueIn.coinUid)
                                add(actionType.valueOut.coinUid)
                            }
                            is TonTransactionRecord.Action.Type.ContractDeploy,
                            is TonTransactionRecord.Action.Type.Unsupported -> Unit
                        }
                    }
                }
                is EvmIncomingTransactionRecord -> listOf(tx.value.coinUid)
                is EvmOutgoingTransactionRecord -> listOf(tx.fee?.coinUid, tx.value.coinUid)
                is SwapTransactionRecord -> listOf(tx.fee, tx.valueIn, tx.valueOut).map { it?.coinUid }
                is UnknownSwapTransactionRecord -> listOf(tx.fee, tx.valueIn, tx.valueOut).map { it?.coinUid }
                is ApproveTransactionRecord -> listOf(tx.fee?.coinUid, tx.value.coinUid)
                is ContractCallTransactionRecord -> {
                    val tempCoinUidList = mutableListOf<String>()
                    tempCoinUidList.addAll(tx.incomingEvents.map { it.value.coinUid })
                    tempCoinUidList.addAll(tx.outgoingEvents.map { it.value.coinUid })
                    tempCoinUidList
                }
                is ExternalContractCallTransactionRecord -> {
                    val tempCoinUidList = mutableListOf<String>()
                    tempCoinUidList.addAll(tx.incomingEvents.map { it.value.coinUid })
                    tempCoinUidList.addAll(tx.outgoingEvents.map { it.value.coinUid })
                    tempCoinUidList
                }
                is BitcoinIncomingTransactionRecord -> listOf(tx.value.coinUid)
                is BitcoinOutgoingTransactionRecord -> listOf(tx.fee, tx.value).map { it?.coinUid }
                is SolanaIncomingTransactionRecord -> listOf(tx.value.coinUid)
                is SolanaOutgoingTransactionRecord -> listOf(tx.fee?.coinUid, tx.value.coinUid)
                is SolanaUnknownTransactionRecord -> {
                    val tempCoinUidList = mutableListOf<String>()
                    tempCoinUidList.addAll(tx.incomingTransfers.map { it.value.coinUid })
                    tempCoinUidList.addAll(tx.outgoingTransfers.map { it.value.coinUid })
                    tempCoinUidList
                }
                is TronOutgoingTransactionRecord -> {
                    listOf(tx.value.coinUid, tx.fee?.coinUid)
                }
                is TronIncomingTransactionRecord -> {
                    listOf(tx.value.coinUid)
                }
                is TronApproveTransactionRecord -> {
                    listOf(tx.value.coinUid, tx.fee?.coinUid)
                }
                is TronContractCallTransactionRecord -> {
                    val tempCoinUidList = mutableListOf<String>()
                    tempCoinUidList.addAll(tx.incomingEvents.map { it.value.coinUid })
                    tempCoinUidList.addAll(tx.outgoingEvents.map { it.value.coinUid })
                    tempCoinUidList
                }
                is TronExternalContractCallTransactionRecord -> {
                    val tempCoinUidList = mutableListOf<String>()
                    tempCoinUidList.addAll(tx.incomingEvents.map { it.value.coinUid })
                    tempCoinUidList.addAll(tx.outgoingEvents.map { it.value.coinUid })
                    tempCoinUidList
                }
                is ZcashShieldingTransactionRecord -> {
                    listOf(tx.value.coinUid)
                }
                else -> emptyList()
            }

            (transactionRecord as? EvmTransactionRecord)?.let { transactionRecord ->
                if (!transactionRecord.foreignTransaction) {
                    coinUids.add(transactionRecord.fee?.coinUid)
                }
            }

            (transactionRecord as? TronTransactionRecord)?.let { transactionRecord ->
                if (!transactionRecord.foreignTransaction) {
                    coinUids.add(transactionRecord.fee?.coinUid)
                }
            }

            coinUids.addAll(txCoinTypes)

            return coinUids.filterNotNull().filter { it.isNotBlank() }.distinct()
        }

    suspend fun start() = withContext(Dispatchers.IO) {
        _transactionInfoItemFlow.update { transactionInfoItem }

        launch {
            adapter.getTransactionRecordsFlowable(null, FilterTransactionType.All, null).asFlow()
                .collect { transactionRecords ->
                    val record = transactionRecords.find { it == transactionRecord }

                    if (record != null) {
                        handleRecordUpdate(record)
                    }
                }
        }

        launch {
            adapter.lastBlockUpdatedFlowable.asFlow()
                .collect {
                    handleLastBlockUpdate()
                }
        }

        launch {
            nftMetadataService.assetsBriefMetadataFlow.collect {
                handleNftMetadata(it)
            }
        }

        fetchRates()
        fetchNftMetadata()
    }

    private suspend fun fetchNftMetadata() {
        val nftUids = transactionRecord.nftUids
        val assetsBriefMetadata = nftMetadataService.assetsBriefMetadata(nftUids)

        handleNftMetadata(assetsBriefMetadata)

        if (nftUids.subtract(assetsBriefMetadata.keys).isNotEmpty()) {
            nftMetadataService.fetch(nftUids)
        }
    }

    private suspend fun fetchRates() = withContext(Dispatchers.IO) {
        val coinUids = coinUidsForRates
        val timestamp = transactionRecord.timestamp

        val rates = coinUids.mapNotNull { coinUid ->
            try {
                val rate = marketKit
                    .coinHistoricalPriceSingle(coinUid, currencyManager.baseCurrency.code, timestamp)
                    .await()
                if (rate != BigDecimal.ZERO) {
                    Pair(coinUid, CurrencyValue(currencyManager.baseCurrency, rate))
                } else {
                    null
                }
            } catch (error: Exception) {
                null
            }
        }.toMap()

        handleRates(rates)
    }

    @Synchronized
    private fun handleLastBlockUpdate() {
        transactionInfoItem = transactionInfoItem.copy(lastBlockInfo = adapter.lastBlockInfo)
    }

    @Synchronized
    private fun handleRecordUpdate(transactionRecord: TransactionRecord) {
        transactionInfoItem = transactionInfoItem.copy(record = transactionRecord)
    }

    @Synchronized
    private fun handleRates(rates: Map<String, CurrencyValue>) {
        transactionInfoItem = transactionInfoItem.copy(rates = rates)
    }

    @Synchronized
    private fun handleNftMetadata(nftMetadata: Map<NftUid, NftAssetBriefMetadata>) {
        transactionInfoItem = transactionInfoItem.copy(nftMetadata = nftMetadata)
    }

    fun getRawTransaction(): String? {
        return adapter.getRawTransaction(transactionRecord.transactionHash)
    }

}
