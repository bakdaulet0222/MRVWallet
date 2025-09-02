package com.mrv.wallet.modules.addtoken

import com.mrv.wallet.core.IAccountManager
import com.mrv.wallet.core.ICoinManager
import com.mrv.wallet.core.IWalletManager
import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.core.order
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.entities.Wallet
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType

class AddTokenService(
    private val coinManager: ICoinManager,
    private val walletManager: IWalletManager,
    private val accountManager: IAccountManager,
    marketKit: MarketKitWrapper,
) {

    private val blockchainTypes = listOf(
        BlockchainType.Ethereum,
        BlockchainType.BinanceSmartChain,
        BlockchainType.Tron,
        BlockchainType.Ton,
        BlockchainType.Polygon,
        BlockchainType.Avalanche,
        BlockchainType.Gnosis,
        BlockchainType.Fantom,
        BlockchainType.ArbitrumOne,
        BlockchainType.Optimism,
        BlockchainType.Base,
        BlockchainType.ZkSync,
        BlockchainType.Solana
    )

    val blockchains = marketKit
        .blockchains(blockchainTypes.map { it.uid })
        .sortedBy { it.type.order }

    val accountType = accountManager.activeAccount?.type

    suspend fun tokenInfo(blockchain: Blockchain, reference: String): TokenInfo? {
        if (reference.isEmpty()) return null

        val blockchainService = when (blockchain.type) {
            BlockchainType.Tron -> {
                AddTronTokenBlockchainService.getInstance(blockchain)
            }
            BlockchainType.Ton -> {
                AddTonTokenBlockchainService(blockchain)
            }
            BlockchainType.Solana -> {
                AddSolanaTokenBlockchainService.getInstance(blockchain)
            }
            else -> AddEvmTokenBlockchainService.getInstance(blockchain)
        }

        if (!blockchainService.isValid(reference)) throw TokenError.InvalidReference

        val token = coinManager.getToken(blockchainService.tokenQuery(reference))
        if (token != null && token.type !is TokenType.Unsupported) {
            return TokenInfo(token, true)
        }

        try {
            val customToken = blockchainService.token(reference)
            return TokenInfo(customToken, false)
        } catch (e: Throwable) {
            throw TokenError.NotFound
        }
    }

    fun addToken(token: TokenInfo) {
        val account = accountManager.activeAccount ?: return
        val wallet = Wallet(token.token, account)
        walletManager.save(listOf(wallet))

        stat(page = StatPage.AddToken, event = StatEvent.AddToken(token.token))
    }

    sealed class TokenError : Exception() {
        object InvalidReference : TokenError()
        object NotFound : TokenError()
    }

    data class TokenInfo(
        val token: Token,
        val inCoinList: Boolean,
    )
}
