package com.mrv.wallet.modules.xtransaction.sections

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mrv.wallet.entities.CurrencyValue
import com.mrv.wallet.entities.TransactionValue
import com.mrv.wallet.modules.amount.AmountInputType
import com.mrv.wallet.modules.fee.HSFeeRaw
import com.mrv.wallet.modules.xtransaction.helpers.TransactionInfoHelper
import com.mrv.wallet.ui.compose.components.cell.SectionUniversalLawrence

@Composable
fun FeeSection(
    transactionInfoHelper: TransactionInfoHelper,
    fee: TransactionValue.CoinValue,
    navController: NavController,
) {
    SectionUniversalLawrence {
        val rateCurrencyValue = transactionInfoHelper.getXRate(fee.coinUid)?.let {
            CurrencyValue(
                currency = transactionInfoHelper.getCurrency(),
                value = it
            )
        }
        HSFeeRaw(
            coinCode = fee.coinCode,
            coinDecimal = fee.decimals,
            fee = fee.value,
            amountInputType = AmountInputType.COIN,
            rate = rateCurrencyValue,
            navController = navController
        )
    }
}