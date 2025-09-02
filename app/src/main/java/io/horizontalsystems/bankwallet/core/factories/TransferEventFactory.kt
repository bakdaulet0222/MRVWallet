package com.mrv.wallet.core.factories

import com.mrv.wallet.core.adapters.StellarTransactionRecord
import com.mrv.wallet.entities.transactionrecords.TransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.EvmIncomingTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.ExternalContractCallTransactionRecord
import com.mrv.wallet.entities.transactionrecords.evm.TransferEvent
import com.mrv.wallet.entities.transactionrecords.tron.TronExternalContractCallTransactionRecord
import com.mrv.wallet.entities.transactionrecords.tron.TronIncomingTransactionRecord

class TransferEventFactory {

    fun transferEvents(transactionRecord: TransactionRecord): List<TransferEvent> {
        return when (transactionRecord) {
            is EvmIncomingTransactionRecord -> {
                listOf(TransferEvent(transactionRecord.from, transactionRecord.value))
            }

            is ExternalContractCallTransactionRecord -> {
                transactionRecord.incomingEvents + transactionRecord.outgoingEvents
            }

            is TronExternalContractCallTransactionRecord -> {
                transactionRecord.incomingEvents + transactionRecord.outgoingEvents
            }

            is TronIncomingTransactionRecord -> {
                listOf(TransferEvent(transactionRecord.from, transactionRecord.value))
            }

            is StellarTransactionRecord -> {
                StellarTransactionRecord.eventsForPhishingCheck(transactionRecord.type)
            }

            else -> {
                listOf()
            }
        }
    }
}