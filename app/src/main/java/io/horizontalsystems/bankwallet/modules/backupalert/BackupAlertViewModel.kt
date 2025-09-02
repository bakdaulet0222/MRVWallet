package com.mrv.wallet.modules.backupalert

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrv.wallet.core.App
import com.mrv.wallet.entities.Account
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BackupAlertViewModel : ViewModel() {
    var account by mutableStateOf<Account?>(null)
        private set

    private var job: Job? = null

    fun resume() {
        job = viewModelScope.launch {
            App.accountManager.newAccountBackupRequiredFlow.collect {
                account = it
            }
        }
    }

    fun pause() {
        job?.cancel()
    }

    fun onHandled() {
        App.accountManager.onHandledBackupRequiredNewAccount()
    }

}