package com.mrv.wallet.modules.market.favorites

import com.mrv.wallet.core.ILocalStorage
import com.mrv.wallet.modules.market.TimeDuration
import com.mrv.wallet.widgets.MarketWidgetManager

class MarketFavoritesMenuService(
    private val localStorage: ILocalStorage,
    private val marketWidgetManager: MarketWidgetManager
) {

    var listSorting: WatchlistSorting
        get() = localStorage.marketFavoritesSorting ?: WatchlistSorting.Manual
        set(value) {
            localStorage.marketFavoritesSorting = value
            marketWidgetManager.updateWatchListWidgets()
        }

    var timeDuration: TimeDuration
        get() = localStorage.marketFavoritesPeriod ?: TimeDuration.OneDay
        set(value) {
            localStorage.marketFavoritesPeriod = value
            marketWidgetManager.updateWatchListWidgets()
        }

    var manualSortOrder: List<String>
        get() = localStorage.marketFavoritesManualSortingOrder
        set(value) {
            localStorage.marketFavoritesManualSortingOrder = value
            marketWidgetManager.updateWatchListWidgets()
        }
}
