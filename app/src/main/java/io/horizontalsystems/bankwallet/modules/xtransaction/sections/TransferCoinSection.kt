package com.mrv.wallet.modules.xtransaction.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatSection
import com.mrv.wallet.entities.TransactionValue
import com.mrv.wallet.modules.xtransaction.cells.AddressCell
import com.mrv.wallet.modules.xtransaction.cells.AmountCellTV
import com.mrv.wallet.modules.xtransaction.cells.AmountColor
import com.mrv.wallet.modules.xtransaction.cells.AmountSign
import com.mrv.wallet.modules.xtransaction.cells.TitleAndValueCell
import com.mrv.wallet.modules.xtransaction.helpers.TransactionInfoHelper
import com.mrv.wallet.ui.compose.components.cell.SectionUniversalLawrence
import io.horizontalsystems.marketkit.models.BlockchainType

@Composable
fun TransferCoinSection(
    amountTitle: String,
    transactionValue: TransactionValue,
    coinAmountColor: AmountColor,
    coinAmountSign: AmountSign,
    addressTitle: String,
    address: String,
    comment: String?,
    statPage: StatPage,
    addressStatSection: StatSection,
    navController: NavController,
    transactionInfoHelper: TransactionInfoHelper,
    blockchainType: BlockchainType,
) {
    SectionUniversalLawrence {
        AmountCellTV(
            title = amountTitle,
            transactionValue = transactionValue,
            coinAmountColor = coinAmountColor,
            coinAmountSign = coinAmountSign,
            transactionInfoHelper = transactionInfoHelper,
            navController = navController,
            statPage = statPage,
            borderTop = false
        )

        val contact = transactionInfoHelper.getContact(address, blockchainType)

        AddressCell(
            title = addressTitle,
            value = address,
            showAddContactButton = contact == null,
            blockchainType = blockchainType,
            statPage = statPage,
            statSection = addressStatSection,
            navController = navController
        )
        contact?.let {
            TitleAndValueCell(
                title = stringResource(R.string.TransactionInfo_ContactName),
                value = it.name
            )
        }
        comment?.let {
            TitleAndValueCell(
                title = stringResource(R.string.TransactionInfo_Memo),
                value = it
            )
        }
    }
}