package com.mrv.wallet.core.managers

import com.mrv.wallet.core.ILocalStorage
import io.horizontalsystems.subscriptions.core.TradeSignals
import io.horizontalsystems.subscriptions.core.UserSubscriptionManager

class SignalsControlManager(
    private val localStorage: ILocalStorage,
) {
    val showSignalsStateChangedFlow = localStorage.marketSignalsStateChangedFlow

    var showSignals: Boolean
        get() = localStorage.marketFavoritesShowSignals
                && UserSubscriptionManager.isActionAllowed(TradeSignals)
        set(value) {
            localStorage.marketFavoritesShowSignals = value
        }
}