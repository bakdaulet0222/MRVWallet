package com.mrv.wallet.modules.send.tron

import com.mrv.wallet.core.HSCaution
import com.mrv.wallet.entities.Address
import com.mrv.wallet.entities.ViewState
import java.math.BigDecimal

data class SendUiState(
    val availableBalance: BigDecimal,
    val amountCaution: HSCaution?,
    val addressError: Throwable?,
    val proceedEnabled: Boolean,
    val sendEnabled: Boolean,
    val feeViewState: ViewState,
    val cautions: List<HSCaution>,
    val showAddressInput: Boolean,
    val address: Address,
)
