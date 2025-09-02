package com.mrv.wallet.modules.multiswap.sendtransaction

import com.mrv.wallet.entities.transactionrecords.bitcoin.BitcoinTransactionRecord
import io.horizontalsystems.ethereumkit.models.FullTransaction

sealed class SendTransactionResult {
    data class Evm(val fullTransaction: FullTransaction) : SendTransactionResult()
    data class Btc(val transactionRecord: BitcoinTransactionRecord?) : SendTransactionResult()
    object Tron : SendTransactionResult()
    object Stellar : SendTransactionResult()
    object Solana : SendTransactionResult()
}
