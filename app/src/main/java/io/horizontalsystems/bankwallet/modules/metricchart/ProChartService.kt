package com.mrv.wallet.modules.metricchart

import com.mrv.wallet.core.managers.CurrencyManager
import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statPage
import com.mrv.wallet.core.stats.statPeriod
import com.mrv.wallet.entities.Currency
import com.mrv.wallet.modules.chart.AbstractChartService
import com.mrv.wallet.modules.chart.ChartPointsWrapper
import io.horizontalsystems.chartview.ChartViewType
import io.horizontalsystems.chartview.models.ChartPoint
import io.horizontalsystems.chartview.models.ChartVolume
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class ProChartService(
    override val currencyManager: CurrencyManager,
    private val marketKit: MarketKitWrapper,
    private val coinUid: String,
    private val chartType: ProChartModule.ChartType
) : AbstractChartService() {

    override val initialChartInterval = HsTimePeriod.Month1
    override val hasVolumes = chartType == ProChartModule.ChartType.TxCount

    override val chartIntervals = when (chartType) {
        ProChartModule.ChartType.CexVolume,
        ProChartModule.ChartType.DexVolume,
        ProChartModule.ChartType.TxCount,
        ProChartModule.ChartType.AddressesCount,
        ProChartModule.ChartType.DexLiquidity -> listOf(
            HsTimePeriod.Week1,
            HsTimePeriod.Week2,
            HsTimePeriod.Month1,
            HsTimePeriod.Month3,
            HsTimePeriod.Month6,
            HsTimePeriod.Year1,
        )
        ProChartModule.ChartType.Tvl -> listOf(
            HsTimePeriod.Day1,
            HsTimePeriod.Week1,
            HsTimePeriod.Week2,
            HsTimePeriod.Month1,
            HsTimePeriod.Month3,
            HsTimePeriod.Month6,
            HsTimePeriod.Year1,
        )
    }

    override val chartViewType = when (chartType) {
        ProChartModule.ChartType.Tvl,
        ProChartModule.ChartType.AddressesCount,
        ProChartModule.ChartType.DexLiquidity -> ChartViewType.Line
        ProChartModule.ChartType.CexVolume,
        ProChartModule.ChartType.DexVolume,
        ProChartModule.ChartType.TxCount -> ChartViewType.Bar
    }

    override fun updateChartInterval(chartInterval: HsTimePeriod?) {
        super.updateChartInterval(chartInterval)

        stat(chartType.statPage, event = StatEvent.SwitchChartPeriod(chartInterval.statPeriod))
    }

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency,
    ): Single<ChartPointsWrapper> {
        val chartDataSingle: Single<List<ChartPoint>> = when (chartType) {
            ProChartModule.ChartType.CexVolume ->
                marketKit.cexVolumesSingle(coinUid, currency.code, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            ChartPoint(
                                value = chartPoint.value.toFloat(),
                                timestamp = chartPoint.timestamp,
                                chartVolume = chartPoint.volume?.toFloat()?.let { ChartVolume(it) }
                            )
                        }
                    }


            ProChartModule.ChartType.DexVolume ->
                marketKit.dexVolumesSingle(coinUid, currency.code, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            ChartPoint(
                                value = chartPoint.volume.toFloat(),
                                timestamp = chartPoint.timestamp,
                            )
                        }
                    }

            ProChartModule.ChartType.DexLiquidity ->
                marketKit.dexLiquiditySingle(coinUid, currency.code, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            ChartPoint(
                                value = chartPoint.volume.toFloat(),
                                timestamp = chartPoint.timestamp,
                            )
                        }
                    }

            ProChartModule.ChartType.TxCount ->
                marketKit.transactionDataSingle(coinUid, chartInterval, null)
                    .map { response ->
                        response.map { chartPoint ->
                            ChartPoint(
                                value = chartPoint.count.toFloat(),
                                timestamp = chartPoint.timestamp,
                                chartVolume = ChartVolume(chartPoint.volume.toFloat()),
                            )
                        }
                    }

            ProChartModule.ChartType.AddressesCount ->
                marketKit.activeAddressesSingle(coinUid, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            ChartPoint(
                                value = chartPoint.count.toFloat(),
                                timestamp = chartPoint.timestamp,
                            )
                        }
                    }

            ProChartModule.ChartType.Tvl ->
                marketKit.marketInfoTvlSingle(coinUid, currency.code, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            ChartPoint(
                                value = chartPoint.value.toFloat(),
                                timestamp = chartPoint.timestamp,
                                chartVolume = chartPoint.volume?.toFloat()?.let { ChartVolume(it) },
                            )
                        }
                    }
        }

        val isMovementChart = when (chartType) {
            ProChartModule.ChartType.DexLiquidity,
            ProChartModule.ChartType.AddressesCount,
            ProChartModule.ChartType.Tvl -> true
            ProChartModule.ChartType.CexVolume,
            ProChartModule.ChartType.DexVolume,
            ProChartModule.ChartType.TxCount -> false
        }

        return chartDataSingle.map { ChartPointsWrapper(it, isMovementChart) }
    }
}
