package com.mrv.wallet.modules.xtransaction.helpers

import androidx.compose.runtime.Composable
import com.mrv.wallet.core.App
import java.math.BigDecimal

@Composable
fun coinAmountString(value: BigDecimal?, coinCode: String, coinDecimals: Int?, sign: String): String {
//    if (hideAmount) return "*****"
    if (value == null) return "---"

    return sign + App.numberFormatter.formatCoinFull(value, coinCode, coinDecimals  ?: 8)
}

@Composable
fun fiatAmountString(value: BigDecimal?, fiatSymbol: String): String {
//    if (hideAmount) return "*****"
    if (value == null) return "---"

    return App.numberFormatter.formatFiatFull(value, fiatSymbol)
}