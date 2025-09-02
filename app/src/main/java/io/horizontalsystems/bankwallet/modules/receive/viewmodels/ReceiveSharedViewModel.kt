package com.mrv.wallet.modules.receive.viewmodels

import androidx.lifecycle.ViewModel
import com.mrv.wallet.core.App
import com.mrv.wallet.entities.Account
import io.horizontalsystems.marketkit.models.FullCoin

class ReceiveSharedViewModel : ViewModel() {

    var coinUid: String? = null

    val activeAccount: Account?
        get() = App.accountManager.activeAccount

    fun fullCoin(): FullCoin? {
        val coinUid = coinUid ?: return null
        return App.marketKit.fullCoins(listOf(coinUid)).firstOrNull()
    }

}