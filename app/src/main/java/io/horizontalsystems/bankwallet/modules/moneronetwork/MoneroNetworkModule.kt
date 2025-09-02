package com.mrv.wallet.modules.moneronetwork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App

object MoneroNetworkModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MoneroNetworkViewModel(App.moneroNodeManager) as T
        }
    }

}
