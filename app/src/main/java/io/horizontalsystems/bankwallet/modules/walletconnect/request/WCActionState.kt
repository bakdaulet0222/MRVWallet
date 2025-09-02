package com.mrv.wallet.modules.walletconnect.request

import com.mrv.wallet.modules.sendevmtransaction.SectionViewItem

data class WCActionState(
    val runnable: Boolean,
    val items: List<SectionViewItem>
)
