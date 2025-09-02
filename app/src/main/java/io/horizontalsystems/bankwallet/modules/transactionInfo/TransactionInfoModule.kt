package com.mrv.wallet.modules.transactionInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.ITransactionsAdapter
import com.mrv.wallet.entities.CurrencyValue
import com.mrv.wallet.entities.LastBlockInfo
import com.mrv.wallet.entities.nft.NftAssetBriefMetadata
import com.mrv.wallet.entities.nft.NftUid
import com.mrv.wallet.entities.transactionrecords.TransactionRecord
import com.mrv.wallet.modules.transactions.NftMetadataService
import io.horizontalsystems.marketkit.models.BlockchainType

object TransactionInfoModule {

    class Factory(val transactionRecord: TransactionRecord) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val transactionSource = transactionRecord.source
            val adapter: ITransactionsAdapter = App.transactionAdapterManager.getAdapter(transactionSource)!!
            val service = TransactionInfoService(
                transactionRecord,
                adapter,
                App.marketKit,
                App.currencyManager,
                NftMetadataService(App.nftMetadataManager),
                App.balanceHiddenManager.balanceHidden,
            )
            val factory = TransactionInfoViewItemFactory(
                transactionSource.blockchain.type.resendable,
                transactionSource.blockchain.type
            )

            return TransactionInfoViewModel(service, factory, App.contactsRepository) as T
        }

    }

    data class ExplorerData(val title: String, val url: String?)
}

sealed class TransactionStatusViewItem(val name: Int) {
    object Pending : TransactionStatusViewItem(R.string.Transactions_Pending)

    //progress in 0.0 .. 1.0
    class Processing(val progress: Float) : TransactionStatusViewItem(R.string.Transactions_Processing)
    object Completed : TransactionStatusViewItem(R.string.Transactions_Completed)
    object Failed : TransactionStatusViewItem(R.string.Transactions_Failed)
}

data class TransactionInfoItem(
    val record: TransactionRecord,
    val lastBlockInfo: LastBlockInfo?,
    val explorerData: TransactionInfoModule.ExplorerData,
    val rates: Map<String, CurrencyValue>,
    val nftMetadata: Map<NftUid, NftAssetBriefMetadata>,
    val hideAmount: Boolean,
)

val BlockchainType.resendable: Boolean
    get() =
        when (this) {
            BlockchainType.Optimism, BlockchainType.Base, BlockchainType.ZkSync, BlockchainType.ArbitrumOne -> false
            else -> true
        }
