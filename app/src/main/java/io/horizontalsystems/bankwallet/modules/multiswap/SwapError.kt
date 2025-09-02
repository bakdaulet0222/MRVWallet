package com.mrv.wallet.modules.multiswap

sealed class SwapError : Throwable() {
    object InsufficientBalanceFrom : SwapError()
}
