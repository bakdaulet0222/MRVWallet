package com.mrv.wallet.modules.xtransaction.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.entities.TransactionValue
import com.mrv.wallet.modules.xtransaction.cells.AmountCellTV
import com.mrv.wallet.modules.xtransaction.cells.AmountColor
import com.mrv.wallet.modules.xtransaction.cells.AmountSign
import com.mrv.wallet.modules.xtransaction.helpers.TransactionInfoHelper
import com.mrv.wallet.ui.compose.components.cell.SectionUniversalLawrence

@Composable
fun MintSection(
    transactionValue: TransactionValue,
    transactionInfoHelper: TransactionInfoHelper,
    navController: NavController,
) {
    SectionUniversalLawrence {
        AmountCellTV(
            title = stringResource(R.string.Send_Confirmation_Mint),
            transactionValue = transactionValue,
            coinAmountColor = AmountColor.Positive,
            coinAmountSign = AmountSign.Plus,
            transactionInfoHelper = transactionInfoHelper,
            navController = navController,
            statPage = StatPage.TonConnect,
            borderTop = false,
        )
    }
}