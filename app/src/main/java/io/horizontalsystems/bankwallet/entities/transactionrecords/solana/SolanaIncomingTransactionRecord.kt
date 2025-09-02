package com.mrv.wallet.entities.transactionrecords.solana

import com.mrv.wallet.entities.TransactionValue
import com.mrv.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.solanakit.models.Transaction

class SolanaIncomingTransactionRecord(
        transaction: Transaction,
        baseToken: Token,
        source: TransactionSource,
        val from: String?,
        val value: TransactionValue
): SolanaTransactionRecord(transaction, baseToken, source) {

    override val mainValue = value

}