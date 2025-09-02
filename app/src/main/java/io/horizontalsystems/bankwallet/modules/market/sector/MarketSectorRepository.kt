package com.mrv.wallet.modules.market.sector

import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.entities.Currency
import com.mrv.wallet.modules.market.MarketItem
import com.mrv.wallet.modules.market.SortingField
import com.mrv.wallet.modules.market.TimeDuration
import com.mrv.wallet.modules.market.favorites.period
import com.mrv.wallet.modules.market.sort
import io.horizontalsystems.marketkit.models.MarketInfo
import io.reactivex.Single
import kotlin.math.min

class MarketSectorRepository(
    private val marketKit: MarketKitWrapper,
) {
    @Volatile
    private var cache: List<MarketInfo> = listOf()

    @Volatile
    private var cacheTimestamp: Long = 0
    private val cacheValidPeriodInMillis = 5_000 // 5 seconds

    @Synchronized
    private fun getMarketItems(coinCategoryUid: String, forceRefresh: Boolean, baseCurrency: Currency): List<MarketInfo> =
        if (forceRefresh && (cacheTimestamp + cacheValidPeriodInMillis < System.currentTimeMillis()) || cache.isEmpty()) {
            val marketInfoList = marketKit.marketInfosSingle(coinCategoryUid, baseCurrency.code).blockingGet()

            cache = marketInfoList
            cacheTimestamp = System.currentTimeMillis()

            marketInfoList
        } else {
            cache
        }

    fun get(
        coinCategoryUid: String,
        size: Int,
        sortingField: SortingField,
        timePeriod: TimeDuration,
        limit: Int,
        baseCurrency: Currency,
        forceRefresh: Boolean
    ): Single<List<MarketItem>> =
        Single.create { emitter ->

            try {
                val marketInfoItems= getMarketItems(coinCategoryUid, forceRefresh, baseCurrency)
                val marketItems = marketInfoItems.map { marketInfo ->
                    MarketItem.createFromCoinMarket(marketInfo, baseCurrency, timePeriod.period)
                }
                val sortedMarketItems = marketItems
                    .subList(0, min(marketItems.size, size))
                    .sort(sortingField)
                    .subList(0, min(marketItems.size, limit))

                emitter.onSuccess(sortedMarketItems)
            } catch (error: Throwable) {
                emitter.onError(error)
            }
        }
}
