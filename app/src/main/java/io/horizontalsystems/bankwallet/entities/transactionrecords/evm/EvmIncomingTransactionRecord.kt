package com.mrv.wallet.entities.transactionrecords.evm

import com.mrv.wallet.entities.TransactionValue
import com.mrv.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.ethereumkit.models.Transaction
import io.horizontalsystems.marketkit.models.Token

class EvmIncomingTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val from: String,
    val value: TransactionValue,
    isSpam: Boolean,
    protected: Boolean
) : EvmTransactionRecord(
    transaction = transaction,
    baseToken = baseToken,
    source = source,
    protected = protected,
    foreignTransaction = true,
    spam = isSpam
) {

    override val mainValue = value

}
