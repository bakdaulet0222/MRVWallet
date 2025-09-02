package com.mrv.wallet.modules.manageaccount.backupconfirmkey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.core.managers.RandomProvider
import com.mrv.wallet.entities.Account

object BackupConfirmKeyModule {
    class Factory(private val account: Account) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackupConfirmKeyViewModel(account, App.accountManager, RandomProvider()) as T
        }
    }
}
