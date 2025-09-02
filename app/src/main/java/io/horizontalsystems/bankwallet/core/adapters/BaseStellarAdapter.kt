package com.mrv.wallet.core.adapters

import com.mrv.wallet.core.IAdapter
import com.mrv.wallet.core.IBalanceAdapter
import com.mrv.wallet.core.IReceiveAdapter
import com.mrv.wallet.core.ISendStellarAdapter
import com.mrv.wallet.core.managers.StellarKitWrapper

abstract class BaseStellarAdapter(
    stellarKitWrapper: StellarKitWrapper
): IAdapter, IBalanceAdapter, IReceiveAdapter, ISendStellarAdapter {
    protected val stellarKit = stellarKitWrapper.stellarKit
    override val receiveAddress: String = stellarKit.receiveAddress

    override val debugInfo: String
        get() = ""

    // IReceiveAdapter

    override val isMainNet = stellarKit.isMainNet

    override suspend fun send(transactionEnvelope: String) {
        stellarKit.sendTransaction(transactionEnvelope)
    }
}
