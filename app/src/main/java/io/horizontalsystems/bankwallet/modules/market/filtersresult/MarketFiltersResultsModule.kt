package com.mrv.wallet.modules.market.filtersresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.core.managers.SignalsControlManager
import com.mrv.wallet.modules.market.filters.IMarketListFetcher

object MarketFiltersResultsModule {
    class Factory(val service: IMarketListFetcher) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val service = MarketFiltersResultService(
                service,
                App.marketFavoritesManager,
                SignalsControlManager(App.localStorage),
                App.marketKit
            )
            return MarketFiltersResultViewModel(service) as T
        }

    }
}
