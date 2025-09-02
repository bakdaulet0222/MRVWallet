package com.mrv.wallet.modules.send.evm.processing

import com.mrv.wallet.core.ViewModelUiState

class SendEvmProcessingViewModel: ViewModelUiState<SendEvmProcessingUiState>() {

    override fun createState(): SendEvmProcessingUiState {
            TODO("Not yet implemented")
    }
}

data class SendEvmProcessingUiState(
    val processing: Boolean
)