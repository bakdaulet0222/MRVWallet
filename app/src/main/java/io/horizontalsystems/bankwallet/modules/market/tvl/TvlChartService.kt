package com.mrv.wallet.modules.market.tvl

import com.mrv.wallet.core.managers.CurrencyManager
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statPeriod
import com.mrv.wallet.entities.Currency
import com.mrv.wallet.modules.chart.AbstractChartService
import com.mrv.wallet.modules.chart.ChartPointsWrapper
import io.horizontalsystems.chartview.ChartViewType
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class TvlChartService(
    override val currencyManager: CurrencyManager,
    private val globalMarketRepository: GlobalMarketRepository
) : AbstractChartService() {

    override val initialChartInterval = HsTimePeriod.Day1

    override val chartIntervals = listOf(
        HsTimePeriod.Day1,
        HsTimePeriod.Week1,
        HsTimePeriod.Week2,
        HsTimePeriod.Month1,
        HsTimePeriod.Month3,
        HsTimePeriod.Month6,
        HsTimePeriod.Year1,
        HsTimePeriod.Year2,
    )
    override val chartViewType = ChartViewType.Line

    var chain: TvlModule.Chain = TvlModule.Chain.All
        set(value) {
            field = value
            dataInvalidated()
        }

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency
    ): Single<ChartPointsWrapper> {
        val chainParam = if (chain == TvlModule.Chain.All) "" else chain.name
        return globalMarketRepository.getTvlGlobalMarketPoints(
            chainParam,
            currency.code,
            chartInterval
        ).map {
            ChartPointsWrapper(it)
        }
    }

    override fun updateChartInterval(chartInterval: HsTimePeriod?) {
        super.updateChartInterval(chartInterval)

        stat(
            page = StatPage.GlobalMetricsTvlInDefi,
            event = StatEvent.SwitchChartPeriod(chartInterval.statPeriod)
        )
    }
}
