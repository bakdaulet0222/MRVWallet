package com.mrv.wallet.modules.send.stellar

import com.mrv.wallet.core.ISendStellarAdapter
import com.mrv.wallet.core.ServiceState
import com.mrv.wallet.entities.Address
import java.math.BigDecimal

class SendStellarMinimumAmountService(
    private val adapter: ISendStellarAdapter
) : ServiceState<SendStellarMinimumAmountService.State>() {

    private var minimumAmount: BigDecimal? = null
    private var error: Throwable? = null

    override fun createState() = State(
        minimumAmount = minimumAmount,
        error = error,
        canBeSend = error == null
    )

    suspend fun setValidAddress(address: Address?) {
        error = null

        try {
            minimumAmount = address?.let {
                adapter.getMinimumSendAmount(it.hex)
            }
        } catch (e: Throwable) {
            minimumAmount = null
            error = e
        }

        emitState()
    }

    data class State(
        val minimumAmount: BigDecimal?,
        val error: Throwable?,
        val canBeSend: Boolean,
    )

}
