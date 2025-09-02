package com.mrv.wallet.modules.send.zcash.shield

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.core.adapters.zcash.ZcashAdapter
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.xrate.XRateService

object ShieldZcashModule {

    class Factory(
        private val wallet: Wallet,
    ) : ViewModelProvider.Factory {
        val adapter = App.adapterManager.getAdapterForWallet<ZcashAdapter>(wallet) ?: throw IllegalStateException("ZcashAdapter is null")

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val xRateService = XRateService(App.marketKit, App.currencyManager.baseCurrency)

            return ShieldZcashViewModel(
                adapter,
                wallet,
                xRateService
            ) as T
        }
    }

}
