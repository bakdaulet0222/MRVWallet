package com.mrv.wallet.core.managers

import com.mrv.wallet.core.storage.TokenAutoEnabledBlockchainDao
import com.mrv.wallet.entities.Account
import com.mrv.wallet.entities.TokenAutoEnabledBlockchain
import io.horizontalsystems.marketkit.models.BlockchainType

class TokenAutoEnableManager(
    private val tokenAutoEnabledBlockchainDao: TokenAutoEnabledBlockchainDao
) {
    fun markAutoEnable(account: Account, blockchainType: BlockchainType) {
        tokenAutoEnabledBlockchainDao.insert(TokenAutoEnabledBlockchain(account.id, blockchainType))
    }

    fun isAutoEnabled(account: Account, blockchainType: BlockchainType): Boolean {
        return tokenAutoEnabledBlockchainDao.get(account.id, blockchainType) != null
    }
}
