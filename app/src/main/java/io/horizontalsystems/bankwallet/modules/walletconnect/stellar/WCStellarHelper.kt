package com.mrv.wallet.modules.walletconnect.stellar

import com.mrv.wallet.R
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.modules.sendevmtransaction.SectionViewItem
import com.mrv.wallet.modules.sendevmtransaction.ValueType
import com.mrv.wallet.modules.sendevmtransaction.ViewItem
import org.stellar.sdk.Transaction
import org.stellar.sdk.operations.Operation

object WCStellarHelper {

    fun getTransactionViewItems(transaction: Transaction, xdr: String, peerName: String): List<SectionViewItem> {
        val operationItems = transaction.operations.map { operation: Operation ->
            ViewItem.Value(
                "Operation",
                operation.javaClass.simpleName,
                ValueType.Regular
            )
        }

        return listOf(
            SectionViewItem(
                operationItems + listOf(
                    ViewItem.Input("Transaction XDR", xdr)
                )
            ),
            SectionViewItem(
                listOf(
                    ViewItem.Value(
                        Translator.getString(R.string.WalletConnect_SignMessageRequest_dApp),
                        peerName,
                        ValueType.Regular
                    ),
                    ViewItem.Value(
                        "Stellar",
                        "",
                        ValueType.Regular
                    )
                )
            )
        )
    }
}
