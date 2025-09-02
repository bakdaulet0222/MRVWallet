package com.mrv.wallet.entities.transactionrecords.evm

import com.mrv.wallet.entities.TransactionValue
import com.mrv.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.ethereumkit.models.Transaction
import io.horizontalsystems.marketkit.models.Token

class UnknownSwapTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val exchangeAddress: String,
    val valueIn: TransactionValue?,
    val valueOut: TransactionValue?,
    protected: Boolean
) : EvmTransactionRecord(transaction, baseToken, source, protected)
