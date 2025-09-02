package com.mrv.wallet.entities.nft

import com.mrv.wallet.entities.Account
import io.horizontalsystems.marketkit.models.BlockchainType

data class NftKey(
    val account: Account,
    val blockchainType: BlockchainType
)