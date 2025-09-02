package com.mrv.wallet.modules.market.topcoins

import com.mrv.wallet.modules.market.SortingField
import com.mrv.wallet.ui.compose.Select

sealed class SelectorDialogState {
    object Closed : SelectorDialogState()
    class Opened(val select: Select<SortingField>) : SelectorDialogState()
}
