package com.mrv.wallet.modules.walletconnect.request

import com.mrv.wallet.core.ServiceState
import com.mrv.wallet.ui.compose.TranslatableString
import kotlinx.coroutines.CoroutineScope

abstract class AbstractWCAction : ServiceState<WCActionState>() {
    abstract fun start(coroutineScope: CoroutineScope)
    abstract suspend fun performAction(): String

    abstract fun getTitle(): TranslatableString
    abstract fun getApproveButtonTitle(): TranslatableString
}
