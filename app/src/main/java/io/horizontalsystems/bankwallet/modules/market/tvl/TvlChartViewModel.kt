package com.mrv.wallet.modules.market.tvl

import com.mrv.wallet.modules.chart.ChartCurrencyValueFormatterShortened
import com.mrv.wallet.modules.chart.ChartViewModel

class TvlChartViewModel(
    private val tvlChartService: TvlChartService,
    chartCurrencyValueFormatter: ChartCurrencyValueFormatterShortened,
) : ChartViewModel(tvlChartService, chartCurrencyValueFormatter) {

    fun onSelectChain(chain: TvlModule.Chain) {
        tvlChartService.chain = chain
    }

}
