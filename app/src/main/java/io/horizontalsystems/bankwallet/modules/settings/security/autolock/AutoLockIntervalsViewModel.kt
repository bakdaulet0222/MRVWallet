package com.mrv.wallet.modules.settings.security.autolock

import androidx.lifecycle.ViewModel
import com.mrv.wallet.core.ILocalStorage

class AutoLockIntervalsViewModel(
    private val localStorage: ILocalStorage
) : ViewModel() {

    private val autoLockInterval: AutoLockInterval
        get() = localStorage.autoLockInterval

    fun onSelectAutoLockInterval(autoLockInterval: AutoLockInterval) {
        localStorage.autoLockInterval = autoLockInterval
    }

    val intervals = AutoLockInterval.values().map {
        AutoLockModule.AutoLockIntervalViewItem(it, it == autoLockInterval)
    }
}