package com.mrv.wallet.entities.transactionrecords.evm

import com.mrv.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.ethereumkit.models.Transaction
import io.horizontalsystems.marketkit.models.Token

class ContractCreationTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    protected: Boolean
) : EvmTransactionRecord(transaction, baseToken, source, protected)
