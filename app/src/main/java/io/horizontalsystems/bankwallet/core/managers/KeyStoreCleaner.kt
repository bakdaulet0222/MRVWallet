package com.mrv.wallet.core.managers

import com.mrv.wallet.core.IAccountManager
import com.mrv.wallet.core.ILocalStorage
import com.mrv.wallet.core.IWalletManager
import io.horizontalsystems.core.IKeyStoreCleaner

class KeyStoreCleaner(
        private val localStorage: ILocalStorage,
        private val accountManager: IAccountManager,
        private val walletManager: IWalletManager)
    : IKeyStoreCleaner {

    override var encryptedSampleText: String?
        get() = localStorage.encryptedSampleText
        set(value) {
            localStorage.encryptedSampleText = value
        }

    override fun cleanApp() {
        accountManager.clear()
        walletManager.clear()
        localStorage.clear()
    }
}
