package com.mrv.wallet.modules.market.sector

import com.mrv.wallet.core.managers.CurrencyManager
import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statPeriod
import com.mrv.wallet.entities.Currency
import com.mrv.wallet.modules.chart.AbstractChartService
import com.mrv.wallet.modules.chart.ChartPointsWrapper
import io.horizontalsystems.chartview.ChartViewType
import io.horizontalsystems.chartview.models.ChartPoint
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class CoinSectorMarketDataChartService(
    override val currencyManager: CurrencyManager,
    private val marketKit: MarketKitWrapper,
    private val categoryUid: String,
) : AbstractChartService() {

    override val initialChartInterval = HsTimePeriod.Day1
    override val chartIntervals = listOf(HsTimePeriod.Day1, HsTimePeriod.Week1, HsTimePeriod.Month1)
    override val chartViewType = ChartViewType.Line

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency
    ): Single<ChartPointsWrapper> = try {
        marketKit.coinCategoryMarketPointsSingle(categoryUid, chartInterval, currency.code)
            .map { info ->
                info.map { ChartPoint(it.marketCap.toFloat(), it.timestamp) }
            }
            .map { ChartPointsWrapper(it) }
    } catch (e: Exception) {
        Single.error(e)
    }

    override fun updateChartInterval(chartInterval: HsTimePeriod?) {
        super.updateChartInterval(chartInterval)

        stat(
            page = StatPage.CoinCategory,
            event = StatEvent.SwitchChartPeriod(chartInterval.statPeriod)
        )
    }
}
