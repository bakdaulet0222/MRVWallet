package com.mrv.wallet.modules.amount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.modules.xrate.XRateService

object AmountInputModeModule {

    class Factory(private val coinUid: String) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            val xRateService = XRateService(App.marketKit, App.currencyManager.baseCurrency)

            return AmountInputModeViewModel(App.localStorage, xRateService, coinUid) as T
        }
    }
}