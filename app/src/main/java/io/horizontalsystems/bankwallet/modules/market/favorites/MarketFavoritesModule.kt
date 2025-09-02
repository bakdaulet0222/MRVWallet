package com.mrv.wallet.modules.market.favorites

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.managers.SignalsControlManager
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.market.MarketItem
import com.mrv.wallet.modules.market.MarketViewItem
import com.mrv.wallet.modules.market.TimeDuration
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.WithTranslatableTitle
import io.horizontalsystems.marketkit.models.Analytics

object MarketFavoritesModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = MarketFavoritesRepository(App.marketKit, App.marketFavoritesManager)
            val menuService = MarketFavoritesMenuService(App.localStorage, App.marketWidgetManager)
            val service = MarketFavoritesService(
                repository,
                menuService,
                App.currencyManager,
                App.backgroundManager,
                App.priceManager,
                SignalsControlManager(App.localStorage)
            )
            return MarketFavoritesViewModel(service) as T
        }
    }

    data class UiState(
        val viewItems: List<MarketViewItem>,
        val viewState: ViewState,
        val isRefreshing: Boolean,
        val sortingField: WatchlistSorting,
        val period: TimeDuration,
        val showSignal: Boolean,
    )

}

enum class WatchlistSorting(@StringRes val titleResId: Int): WithTranslatableTitle {
    Manual(R.string.Market_Sorting_Manual),
    HighestCap(R.string.Market_Sorting_HighestCap),
    LowestCap(R.string.Market_Sorting_LowestCap),
    Gainers(R.string.Market_Sorting_Gainers),
    Losers(R.string.Market_Sorting_Losers);

    override val title = TranslatableString.ResString(titleResId)
}

data class MarketItemWrapper(
    val marketItem: MarketItem,
    val favorited: Boolean,
    val signal: Analytics.TechnicalAdvice.Advice? =  null
)