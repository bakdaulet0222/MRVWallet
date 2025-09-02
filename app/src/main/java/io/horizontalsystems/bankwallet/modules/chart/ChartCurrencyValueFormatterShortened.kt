package com.mrv.wallet.modules.chart

import com.mrv.wallet.core.App
import com.mrv.wallet.entities.Currency
import java.math.BigDecimal

class ChartCurrencyValueFormatterShortened : ChartModule.ChartNumberFormatter {

    override fun formatValue(currency: Currency, value: BigDecimal): String {
        return App.numberFormatter.formatFiatShort(value, currency.symbol, 2)
    }

}
