package com.mrv.wallet.entities.transactionrecords.evm

import com.mrv.wallet.entities.TransactionValue

data class TransferEvent(
    val address: String?,
    val value: TransactionValue
)
