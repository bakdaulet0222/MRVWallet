package com.mrv.wallet.modules.send

import com.mrv.wallet.core.HSCaution
import com.mrv.wallet.entities.Address
import java.math.BigDecimal

data class SendUiState(
    val availableBalance: BigDecimal,
    val amountCaution: HSCaution?,
    val canBeSend: Boolean,
    val showAddressInput: Boolean,
    val address: Address,
)
