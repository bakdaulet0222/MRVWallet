package com.mrv.wallet.modules.walletconnect.stellar

import com.walletconnect.android.Core
import com.walletconnect.web3.wallet.client.Wallet
import com.mrv.wallet.core.App
import com.mrv.wallet.core.managers.StellarKitManager
import com.mrv.wallet.entities.Account
import com.mrv.wallet.modules.walletconnect.handler.IWCHandler
import com.mrv.wallet.modules.walletconnect.handler.MethodData
import com.mrv.wallet.modules.walletconnect.handler.UnsupportedMethodException
import com.mrv.wallet.modules.walletconnect.request.AbstractWCAction
import io.horizontalsystems.stellarkit.StellarKit

class WCHandlerStellar(private val stellarKitManager: StellarKitManager) : IWCHandler {
    override val chainNamespace = "stellar"

    override val supportedChains = listOf("stellar:pubnet")
    override val supportedMethods = listOf("stellar_signAndSubmitXDR", "stellar_signXDR")
    override val supportedEvents = listOf<String>()

    override fun getAction(
        request: Wallet.Model.SessionRequest.JSONRPCRequest,
        peerMetaData: Core.Model.AppMetaData?,
        chainInternalId: String?,
    ): AbstractWCAction {
        val stellarKit = getStellarKit(App.accountManager.activeAccount!!)

        return when (request.method) {
            "stellar_signAndSubmitXDR" -> WCActionStellarSignAndSubmitXdr(
                request.params,
                peerMetaData?.name ?: "",
                stellarKit
            )

            "stellar_signXDR" -> WCActionStellarSignXdr(
                request.params,
                peerMetaData?.name ?: "",
                stellarKit
            )

            else -> throw UnsupportedMethodException(request.method)
        }
    }

    private fun getStellarKit(account: Account): StellarKit {
        return stellarKitManager.getStellarKitWrapper(account).stellarKit
    }

    override fun getAccountAddresses(account: Account): List<String> {
        val address = stellarKitManager.getAddress(account.type)

        return supportedChains.map { "$it:$address" }
    }

    override fun getMethodData(method: String, chainInternalId: String?): MethodData {
        val title = when (method) {
            "stellar_signAndSubmitXDR" -> "Approve Transaction"
            "stellar_signXDR" -> "Sign Request"
            else -> method
        }

        return MethodData(title, "Stellar")
    }

    override fun getChainName(chainInternalId: String) = "Stellar"
}
