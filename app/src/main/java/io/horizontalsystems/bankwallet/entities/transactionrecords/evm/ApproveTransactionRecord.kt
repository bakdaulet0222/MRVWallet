package com.mrv.wallet.entities.transactionrecords.evm

import com.mrv.wallet.entities.TransactionValue
import com.mrv.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.ethereumkit.models.Transaction
import io.horizontalsystems.marketkit.models.Token

class ApproveTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val spender: String,
    val value: TransactionValue,
    protected: Boolean
) : EvmTransactionRecord(transaction, baseToken, source, protected) {

    override val mainValue = value

}
