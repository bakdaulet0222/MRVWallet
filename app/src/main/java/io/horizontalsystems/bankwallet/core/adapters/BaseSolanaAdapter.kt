package com.mrv.wallet.core.adapters

import com.mrv.wallet.core.IAdapter
import com.mrv.wallet.core.IBalanceAdapter
import com.mrv.wallet.core.IReceiveAdapter
import com.mrv.wallet.core.ISendSolanaAdapter
import com.mrv.wallet.core.managers.SolanaKitWrapper
import io.horizontalsystems.solanakit.Signer
import io.horizontalsystems.solanakit.models.FullTransaction
import java.math.BigDecimal

abstract class BaseSolanaAdapter(
        solanaKitWrapper: SolanaKitWrapper,
        val decimal: Int
) : IAdapter, IBalanceAdapter, IReceiveAdapter, ISendSolanaAdapter {

    val solanaKit = solanaKitWrapper.solanaKit
    protected val signer: Signer? = solanaKitWrapper.signer

    override val debugInfo: String
        get() = solanaKit.debugInfo()

    val statusInfo: Map<String, Any>
        get() = solanaKit.statusInfo()

    // IReceiveAdapter

    override val receiveAddress: String
        get() = solanaKit.receiveAddress

    override val isMainNet: Boolean
        get() = solanaKit.isMainnet

    override fun estimateFee(rawTransaction: ByteArray): BigDecimal {
        return solanaKit.estimateFee(rawTransaction)
    }

    override suspend fun send(rawTransaction: ByteArray): FullTransaction {
        if (signer == null) throw Exception()

        return solanaKit.sendRawTransaction(rawTransaction, signer)
    }

    companion object {
        const val confirmationsThreshold: Int = 12
    }

}
