package com.mrv.wallet.modules.moneronetwork.addnode

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mrv.wallet.R
import com.mrv.wallet.core.Caution
import com.mrv.wallet.core.managers.MoneroNodeManager
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import io.horizontalsystems.marketkit.models.BlockchainType
import java.net.MalformedURLException
import java.net.URI

class AddMoneroNodeViewModel(
    private val nodeManager: MoneroNodeManager
) : ViewModel() {

    private var url = ""
    private var username: String? = null
    private var password: String? = null
    private var urlCaution: Caution? = null

    var viewState by mutableStateOf(AddMoneroNodeViewState(null))
        private set

    fun onEnterUsername(username: String) {
        this.username = username.trim()
    }

    fun onEnterPassword(password: String) {
        this.password = password
    }

    fun onEnterRpcUrl(enteredUrl: String) {
        urlCaution = null
        url = enteredUrl.trim()
        syncState()
    }

    fun onScreenClose() {
        viewState = AddMoneroNodeViewState()
    }

    fun onAddClick() {
        val sourceUri: URI

        try {
            sourceUri = URI(url)
            val hasRequiredProtocol = listOf("https").contains(sourceUri.scheme)
            if (!hasRequiredProtocol) {
                throw MalformedURLException()
            }
        } catch (e: Throwable) {
            urlCaution = Caution(Translator.getString(R.string.AddMoneroNode_Error_InvalidUrl), Caution.Type.Error)
            syncState()
            return
        }

        if (nodeManager.allNodes.any { it.host == url }) {
            urlCaution = Caution(Translator.getString(R.string.AddMoneroNode_Warning_UrlExists), Caution.Type.Warning)
            syncState()
            return
        }

        nodeManager.addMoneroNode(url, username, password)

        viewState = AddMoneroNodeViewState(null, true)

        stat(
            page = StatPage.BlockchainSettingsEvmAdd,
            event = StatEvent.AddEvmSource(BlockchainType.Monero.uid)
        )
    }

    private fun syncState() {
        viewState = AddMoneroNodeViewState(urlCaution)
    }
}

data class AddMoneroNodeViewState(
    val urlCaution: Caution? = null,
    val closeScreen: Boolean = false
)
