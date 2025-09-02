package com.mrv.wallet.core.storage

import com.mrv.wallet.entities.SpamAddress
import com.mrv.wallet.entities.SpamScanState
import io.horizontalsystems.marketkit.models.BlockchainType

class SpamAddressStorage(
    private val spamAddressDao: SpamAddressDao
) {

    fun isSpam(hash: ByteArray): Boolean =
        spamAddressDao.getByTransaction(hash) != null

    fun findByAddress(address: String): SpamAddress? =
        spamAddressDao.getByAddress(address)

    fun save(spamAddresses: List<SpamAddress>) {
        spamAddressDao.insertAll(spamAddresses)
    }

    fun save(spamScanState: SpamScanState) {
        spamAddressDao.insert(spamScanState)
    }

    fun getSpamScanState(blockchainType: BlockchainType, accountUid: String): SpamScanState? =
        spamAddressDao.getSpamScanState(blockchainType, accountUid)
}
