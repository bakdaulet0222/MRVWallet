package com.mrv.wallet.modules.rooteddevice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mrv.wallet.core.ILocalStorage
import com.mrv.wallet.core.utils.RootUtil

class RootedDeviceViewModel(
    private val localStorage: ILocalStorage,
    rootUtil: RootUtil,
): ViewModel() {

    var showRootedDeviceWarning by mutableStateOf(!localStorage.ignoreRootedDeviceWarning && rootUtil.isRooted)
        private set

    fun ignoreRootedDeviceWarning() {
        localStorage.ignoreRootedDeviceWarning = true
        showRootedDeviceWarning = false
    }

}
