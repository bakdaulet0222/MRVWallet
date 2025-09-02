package com.mrv.wallet.modules.xrate

import androidx.lifecycle.ViewModel
import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.entities.Currency
import com.mrv.wallet.entities.CurrencyValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow

class XRateService(
    private val marketKit: MarketKitWrapper,
    private val currency: Currency
) : ViewModel() {

    fun getRate(coinUid: String): CurrencyValue? {
        return marketKit.coinPrice(coinUid, currency.code)?.let {
            CurrencyValue(currency, it.value)
        }
    }

    fun getRateFlow(coinUid: String): Flow<CurrencyValue> {
        return marketKit.coinPriceObservable("xrate-service", coinUid, currency.code).asFlow()
            .map {
                CurrencyValue(currency, it.value)
            }
    }
}
