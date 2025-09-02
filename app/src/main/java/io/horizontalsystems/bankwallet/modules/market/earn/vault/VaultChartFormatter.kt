package com.mrv.wallet.modules.market.earn.vault

import com.mrv.wallet.core.App
import com.mrv.wallet.entities.Currency
import com.mrv.wallet.modules.chart.ChartModule
import java.math.BigDecimal

class VaultChartFormatter : ChartModule.ChartNumberFormatter {

    override fun formatValue(currency: Currency, value: BigDecimal): String {
        return App.numberFormatter.format(value, 0, 2, "APY ", "%")
    }

    override fun formatMinMaxValue(
        currency: Currency,
        value: BigDecimal
    ): String {
        return App.numberFormatter.format(value, 0, 2, "", "%")
    }
}
