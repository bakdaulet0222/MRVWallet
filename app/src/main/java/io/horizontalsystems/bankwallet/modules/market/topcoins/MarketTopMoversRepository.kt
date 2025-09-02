package com.mrv.wallet.modules.market.topcoins

import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.entities.Currency
import io.horizontalsystems.marketkit.models.TopMovers
import io.reactivex.Single

class MarketTopMoversRepository(
    private val marketKit: MarketKitWrapper
) {

    fun getTopMovers(baseCurrency: Currency): Single<TopMovers> =
        marketKit.topMoversSingle(baseCurrency.code)

}
