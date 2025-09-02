package com.mrv.wallet.core.adapters

import com.mrv.wallet.core.IAdapter
import com.mrv.wallet.core.IBalanceAdapter
import com.mrv.wallet.core.IReceiveAdapter
import com.mrv.wallet.core.managers.TonKitWrapper
import com.mrv.wallet.core.managers.statusInfo
import io.horizontalsystems.tonkit.models.Network

abstract class BaseTonAdapter(
    tonKitWrapper: TonKitWrapper,
    val decimals: Int
) : IAdapter, IBalanceAdapter, IReceiveAdapter {

    protected val tonKit = tonKitWrapper.tonKit

    override val debugInfo: String
        get() = ""

    val statusInfo: Map<String, Any>
        get() = tonKit.statusInfo()

    // IReceiveAdapter

    override val receiveAddress: String
        get() = tonKit.receiveAddress.toUserFriendly(false)

    override val isMainNet: Boolean
        get() = tonKit.network == Network.MainNet

    // ISendTronAdapter
}
