package com.mrv.wallet.core.factories

import com.mrv.wallet.core.IAccountManager
import com.mrv.wallet.core.IWalletManager
import com.mrv.wallet.core.managers.EvmAccountManager
import com.mrv.wallet.core.managers.EvmKitManager
import com.mrv.wallet.core.managers.MarketKitWrapper
import com.mrv.wallet.core.managers.TokenAutoEnableManager
import io.horizontalsystems.marketkit.models.BlockchainType

class EvmAccountManagerFactory(
    private val accountManager: IAccountManager,
    private val walletManager: IWalletManager,
    private val marketKit: MarketKitWrapper,
    private val tokenAutoEnableManager: TokenAutoEnableManager
) {

    fun evmAccountManager(blockchainType: BlockchainType, evmKitManager: EvmKitManager) =
        EvmAccountManager(
            blockchainType,
            accountManager,
            walletManager,
            marketKit,
            evmKitManager,
            tokenAutoEnableManager
        )

}
