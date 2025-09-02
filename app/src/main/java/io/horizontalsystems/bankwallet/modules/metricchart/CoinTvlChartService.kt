package com.mrv.wallet.modules.metricchart

import com.mrv.wallet.core.managers.CurrencyManager
import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.entities.Currency
import com.mrv.wallet.modules.chart.AbstractChartService
import com.mrv.wallet.modules.chart.ChartPointsWrapper
import io.horizontalsystems.chartview.ChartViewType
import io.horizontalsystems.chartview.models.ChartPoint
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class CoinTvlChartService(
    override val currencyManager: CurrencyManager,
    private val marketKit: MarketKitWrapper,
    private val coinUid: String,
) : AbstractChartService() {

    override val initialChartInterval = HsTimePeriod.Month1
    override val chartIntervals = HsTimePeriod.values().toList()
    override val chartViewType = ChartViewType.Line

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency
    ): Single<ChartPointsWrapper> = try {
        marketKit.marketInfoTvlSingle(coinUid, currency.code, chartInterval)
            .map { info ->
                info.map { ChartPoint(it.value.toFloat(), it.timestamp) }
            }
            .map { ChartPointsWrapper(it) }
    } catch (e: Exception) {
        Single.error(e)
    }

}
