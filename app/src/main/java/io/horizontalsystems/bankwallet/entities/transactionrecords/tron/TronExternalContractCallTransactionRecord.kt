package com.mrv.wallet.entities.transactionrecords.tron

import com.mrv.wallet.entities.TransactionValue
import com.mrv.wallet.entities.transactionrecords.evm.EvmTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.TransferEvent
import com.mrv.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.tronkit.models.Transaction

class TronExternalContractCallTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val incomingEvents: List<TransferEvent>,
    val outgoingEvents: List<TransferEvent>,
    spam: Boolean
) : TronTransactionRecord(
    transaction = transaction,
    baseToken = baseToken,
    source = source,
    foreignTransaction = true,
    spam = spam
) {

    override val mainValue: TransactionValue?
        get() {
            val (incomingValues, outgoingValues) = EvmTransactionRecord.combined(incomingEvents, outgoingEvents)

            return when {
                (incomingValues.isEmpty() && outgoingValues.size == 1) -> outgoingValues.first()
                (incomingValues.size == 1 && outgoingValues.isEmpty()) -> incomingValues.first()
                else -> null
            }
        }

}
