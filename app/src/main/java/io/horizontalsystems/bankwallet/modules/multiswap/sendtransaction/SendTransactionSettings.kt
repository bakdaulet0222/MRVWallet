package com.mrv.wallet.modules.multiswap.sendtransaction

import com.mrv.wallet.modules.evmfee.GasPriceInfo
import io.horizontalsystems.ethereumkit.models.Address

sealed class SendTransactionSettings {
    data class Evm(val gasPriceInfo: GasPriceInfo?, val receiveAddress: Address) : SendTransactionSettings()
    class Btc : SendTransactionSettings()
    class Tron : SendTransactionSettings()
    class Stellar : SendTransactionSettings()
}
