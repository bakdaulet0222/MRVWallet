package com.mrv.wallet.modules.multiswap

import com.mrv.wallet.core.ServiceState
import com.mrv.wallet.core.managers.MarketKitWrapper
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType

class SwapDefaultTokenService(
    private val marketKit: MarketKitWrapper
) : ServiceState<SwapDefaultTokenState>() {
    private var tokenOut: Token? = null

    override fun createState() = SwapDefaultTokenState(
        tokenOut = tokenOut
    )

    fun setTokenIn(token: Token) {
        determineTokenOut(token)

        emitState()
    }

    private fun determineTokenOut(token: Token) {
        val blockchainType = token.blockchainType

        if (token.type == TokenType.Native) {
            val coinUid = when (blockchainType) {
                BlockchainType.BinanceSmartChain -> "binance-bridged-usdt-bnb-smart-chain"
                else -> "tether"
            }

            tokenOut = marketKit.fullCoins(listOf(coinUid))
                .firstOrNull()
                ?.let { fullCoin ->
                    fullCoin.tokens.firstOrNull { it.blockchainType == blockchainType }
                }
        } else {
            tokenOut = marketKit.token(TokenQuery(blockchainType, TokenType.Native))
        }
    }
}

data class SwapDefaultTokenState(val tokenOut: Token?)
