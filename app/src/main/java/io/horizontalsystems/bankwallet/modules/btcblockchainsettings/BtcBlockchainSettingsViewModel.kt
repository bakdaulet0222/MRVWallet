package com.mrv.wallet.modules.btcblockchainsettings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrv.wallet.R
import com.mrv.wallet.core.imageUrl
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.entities.BtcRestoreMode
import com.mrv.wallet.modules.btcblockchainsettings.BtcBlockchainSettingsModule.BlockchainSettingsIcon
import com.mrv.wallet.modules.btcblockchainsettings.BtcBlockchainSettingsModule.ViewItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class BtcBlockchainSettingsViewModel(
    private val service: BtcBlockchainSettingsService
) : ViewModel() {

    var closeScreen by mutableStateOf(false)
        private set

    var restoreSources by mutableStateOf<List<ViewItem>>(listOf())
        private set

    var saveButtonEnabled by mutableStateOf(false)
        private set

    val title: String = service.blockchain.name
    val blockchainIconUrl = service.blockchain.type.imageUrl

    init {
        viewModelScope.launch {
            service.hasChangesObservable.asFlow().collect {
                saveButtonEnabled = it
                syncRestoreModeState()
            }
        }

        syncRestoreModeState()
    }

    fun onSelectRestoreMode(viewItem: ViewItem) {
        service.setRestoreMode(viewItem.id)
    }

    fun onSaveClick() {
        service.save()
        closeScreen = true
    }

    private fun syncRestoreModeState() {
        val viewItems = service.restoreModes.map { mode ->
            ViewItem(
                id = mode.raw,
                title = Translator.getString(mode.title),
                subtitle = Translator.getString(mode.description),
                selected = mode == service.restoreMode,
                icon = mode.icon
            )
        }
        restoreSources = viewItems
    }

    private val BtcRestoreMode.icon: BlockchainSettingsIcon
        get() = when (this) {
            BtcRestoreMode.Blockchair -> BlockchainSettingsIcon.ApiIcon(R.drawable.ic_blockchair)
            BtcRestoreMode.Hybrid -> BlockchainSettingsIcon.ApiIcon(R.drawable.ic_api_hybrid)
            BtcRestoreMode.Blockchain -> BlockchainSettingsIcon.BlockchainIcon(service.blockchain.type.imageUrl)
        }

}
