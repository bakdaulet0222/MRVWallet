package com.mrv.wallet.modules.coin.coinmarkets

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.MarketTickerViewItem
import com.mrv.wallet.ui.compose.Select
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.WithTranslatableTitle
import io.horizontalsystems.marketkit.models.FullCoin

object CoinMarketsModule {
    class Factory(private val fullCoin: FullCoin) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CoinMarketsViewModel(fullCoin, App.currencyManager.baseCurrency, App.marketKit) as T
        }
    }

    sealed class VolumeMenuType : WithTranslatableTitle {
        class Coin(val name: String) : VolumeMenuType()
        class Currency(val name: String) : VolumeMenuType()

        override val title: TranslatableString
            get() = when (this) {
                is Coin -> TranslatableString.PlainString(name)
                is Currency -> TranslatableString.PlainString(name)
            }
    }

    data class CoinMarketUiState(
        val verified: Boolean,
        val viewState: ViewState,
        val exchangeTypeMenu: Select<ExchangeType>,
        val items: List<MarketTickerViewItem>,
    )

    enum class ExchangeType(@StringRes val titleResId: Int) : WithTranslatableTitle {
        ALL(R.string.CoinPage_MarketsVerifiedMenu_All),
        CEX(R.string.CoinPage_MarketsVerifiedMenu_Cex),
        DEX(R.string.CoinPage_MarketsVerifiedMenu_Dex);

        override val title: TranslatableString
            get() = TranslatableString.ResString(titleResId)
    }
}
