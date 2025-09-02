package com.mrv.wallet.modules.chart

import com.mrv.wallet.entities.Currency
import com.mrv.wallet.modules.coin.overview.ui.SelectedItem
import com.mrv.wallet.modules.market.Value
import io.horizontalsystems.chartview.models.ChartVolumeType
import java.math.BigDecimal

object ChartModule {

    fun createViewModel(
        chartService: AbstractChartService,
        chartNumberFormatter: ChartNumberFormatter,
    ): ChartViewModel {
        return ChartViewModel(chartService, chartNumberFormatter)
    }

    interface ChartNumberFormatter {
        fun formatValue(currency: Currency, value: BigDecimal): String
        fun formatMinMaxValue(currency: Currency, value: BigDecimal): String {
            return formatValue(currency, value)
        }
    }

    data class ChartHeaderView(
        val value: String,
        val valueHint: String?,
        val date: String?,
        val diff: Value.Percent?,
        val extraData: ChartHeaderExtraData?
    )

    sealed class ChartHeaderExtraData {
        class Volume(val volume: String, val type: ChartVolumeType) : ChartHeaderExtraData()
        class Dominance(val dominance: String, val diff: Value.Percent?) : ChartHeaderExtraData()
        class Indicators(
            val movingAverages: List<SelectedItem.MA>,
            val rsi: Float?,
            val macd: SelectedItem.Macd?
        ) : ChartHeaderExtraData()
    }

}
