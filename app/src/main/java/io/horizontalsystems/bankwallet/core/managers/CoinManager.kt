package com.mrv.wallet.core.managers

import com.mrv.wallet.core.ICoinManager
import com.mrv.wallet.core.IWalletManager
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenQuery

class CoinManager(
    private val marketKit: MarketKitWrapper,
    private val walletManager: IWalletManager
) : ICoinManager {

    override fun getToken(query: TokenQuery): Token? {
        return marketKit.token(query) ?: customToken(query)
    }

    private fun customToken(tokenQuery: TokenQuery): Token? {
        return walletManager.activeWallets.find { it.token.tokenQuery == tokenQuery }?.token
    }
}
