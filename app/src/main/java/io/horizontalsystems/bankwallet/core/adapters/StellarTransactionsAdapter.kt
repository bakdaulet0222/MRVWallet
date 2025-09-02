package com.mrv.wallet.core.adapters

import com.mrv.wallet.core.AdapterState
import com.mrv.wallet.core.ITransactionsAdapter
import com.mrv.wallet.core.adapters.TonTransactionsAdapter.NotSupportedException
import com.mrv.wallet.core.factories.StellarTransactionConverter
import com.mrv.wallet.core.managers.StellarKitWrapper
import com.mrv.wallet.core.managers.toAdapterState
import com.mrv.wallet.entities.LastBlockInfo
import com.mrv.wallet.entities.transactionrecords.TransactionRecord
import com.mrv.wallet.modules.transactions.FilterTransactionType
import com.mrv.wallet.modules.transactions.FilterTransactionType.All
import com.mrv.wallet.modules.transactions.FilterTransactionType.Approve
import com.mrv.wallet.modules.transactions.FilterTransactionType.Incoming
import com.mrv.wallet.modules.transactions.FilterTransactionType.Outgoing
import com.mrv.wallet.modules.transactions.FilterTransactionType.Swap
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.stellarkit.TagQuery
import io.horizontalsystems.stellarkit.room.StellarAsset
import io.horizontalsystems.stellarkit.room.Tag
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlowable
import kotlinx.coroutines.rx2.rxSingle

class StellarTransactionsAdapter(
    stellarKitWrapper: StellarKitWrapper,
    private val transactionConverter: StellarTransactionConverter,
) : ITransactionsAdapter {
    private val stellarKit = stellarKitWrapper.stellarKit

    override val explorerTitle = "Stellar Expert"
    override val transactionsState: AdapterState
        get() = stellarKit.operationsSyncStateFlow.value.toAdapterState()
    override val transactionsStateUpdatedFlowable: Flowable<Unit>
        get() = stellarKit.operationsSyncStateFlow.asFlowable().map {}
    override val lastBlockInfo: LastBlockInfo?
        get() = null
    override val lastBlockUpdatedFlowable: Flowable<Unit>
        get() = Flowable.empty()

    override fun getTransactionsAsync(
        from: TransactionRecord?,
        token: Token?,
        limit: Int,
        transactionType: FilterTransactionType,
        address: String?,
    ): Single<List<TransactionRecord>> = try {
        val tagQuery = getTagQuery(token, transactionType, address)
        val beforeId = (from as StellarTransactionRecord?)?.operation?.id

        rxSingle {
            stellarKit.operationsBefore(tagQuery, fromId = beforeId, limit = limit)
                .map {
                    transactionConverter.convert(it)
                }
        }
    } catch (e: NotSupportedException) {
        Single.just(listOf())
    }

    override fun getTransactionsAfter(fromTransactionId: String?): Single<List<TransactionRecord>> {
        return rxSingle {
            stellarKit.operationsAfter(TagQuery(null, null, null), fromTransactionId?.toLongOrNull(), 10000)
                .map {
                    transactionConverter.convert(it)
                }
        }
    }

    private fun getTagQuery(
        token: Token?,
        transactionType: FilterTransactionType,
        address: String?,
    ): TagQuery {
        var assetId: String? = null

        val tokenType = token?.type

        if (tokenType == TokenType.Native) {
            assetId = StellarAsset.Native.id
        } else if (tokenType is TokenType.Asset) {
            assetId = StellarAsset.Asset(tokenType.code, tokenType.issuer).id
        }

        val tagType = when (transactionType) {
            All -> null
            Incoming -> Tag.Type.Incoming
            Outgoing -> Tag.Type.Outgoing
            Swap -> Tag.Type.Swap
            Approve -> throw NotSupportedException()
        }

        return TagQuery(
            tagType,
            assetId,
            address
        )
    }

    override fun getTransactionRecordsFlowable(
        token: Token?,
        transactionType: FilterTransactionType,
        address: String?,
    ): Flowable<List<TransactionRecord>> = try {
        val tagQuery = getTagQuery(token, transactionType, address)

        stellarKit
            .operationFlow(tagQuery)
            .map { operationInfo ->
                operationInfo.operations.map {
                    transactionConverter.convert(it)
                }
            }
            .asFlowable()
    } catch (e: NotSupportedException) {
        Flowable.empty()
    }

    override fun getTransactionUrl(transactionHash: String): String {
        return "https://stellar.expert/explorer/public/tx/${transactionHash}"
    }
}