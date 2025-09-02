package com.mrv.wallet.modules.multiswap.providers

import com.mrv.wallet.modules.multiswap.ISwapFinalQuote
import com.mrv.wallet.modules.multiswap.ISwapQuote
import com.mrv.wallet.modules.multiswap.sendtransaction.SendTransactionSettings
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import java.math.BigDecimal

interface IMultiSwapProvider {
    val id: String
    val title: String
    val url: String
    val icon: Int
    val priority: Int

    suspend fun start() = Unit

    fun supports(tokenFrom: Token, tokenTo: Token): Boolean {
        return tokenFrom.blockchainType == tokenTo.blockchainType &&
            supports(tokenFrom.blockchainType)
    }

    fun supports(blockchainType: BlockchainType): Boolean
    suspend fun fetchQuote(
        tokenIn: Token,
        tokenOut: Token,
        amountIn: BigDecimal,
        settings: Map<String, Any?>
    ): ISwapQuote

    suspend fun fetchFinalQuote(
        tokenIn: Token,
        tokenOut: Token,
        amountIn: BigDecimal,
        swapSettings: Map<String, Any?>,
        sendTransactionSettings: SendTransactionSettings?,
        swapQuote: ISwapQuote
    ) : ISwapFinalQuote
}
