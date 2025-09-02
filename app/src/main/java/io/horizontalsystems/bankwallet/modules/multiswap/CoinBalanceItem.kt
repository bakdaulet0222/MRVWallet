package com.mrv.wallet.modules.multiswap

import com.mrv.wallet.entities.CurrencyValue
import io.horizontalsystems.marketkit.models.Token
import java.math.BigDecimal

data class CoinBalanceItem(
    val token: Token,
    val balance: BigDecimal?,
    val fiatBalanceValue: CurrencyValue?,
)
