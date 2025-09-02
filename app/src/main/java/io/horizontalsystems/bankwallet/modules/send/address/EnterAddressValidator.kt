package com.mrv.wallet.modules.send.address

import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.IAdapterManager
import com.mrv.wallet.core.ISendBitcoinAdapter
import com.mrv.wallet.core.ISendStellarAdapter
import com.mrv.wallet.core.ISendTronAdapter
import com.mrv.wallet.core.ISendZcashAdapter
import com.mrv.wallet.core.adapters.zcash.ZcashAdapter.ZcashError
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.entities.Address
import io.horizontalsystems.ethereumkit.core.AddressValidator
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.monerokit.MoneroKit
import io.horizontalsystems.tonkit.FriendlyAddress

interface EnterAddressValidator {
    @Throws
    suspend fun validate(address: Address)
}

class BitcoinAddressValidator(
    private val token: Token,
    private val adapterManager: IAdapterManager
) : EnterAddressValidator {
    private val sendAdapter by lazy { adapterManager.getAdapterForToken<ISendBitcoinAdapter>(token) }

    override suspend fun validate(address: Address) {
        val adapter = sendAdapter ?: throw AddressValidationError.NoAdapter()

        adapter.validate(address.hex, null)
    }
}

class EvmAddressValidator : EnterAddressValidator {
    override suspend fun validate(address: Address) {
        AddressValidator.validate(address.hex)
        io.horizontalsystems.ethereumkit.models.Address(address.hex)
    }
}

class SolanaAddressValidator : EnterAddressValidator {
    override suspend fun validate(address: Address) {
        io.horizontalsystems.solanakit.models.Address(address.hex)
    }
}

class TonAddressValidator : EnterAddressValidator {
    override suspend fun validate(address: Address) {
        FriendlyAddress.parse(address.hex)
    }
}

class StellarAddressValidator(private val token: Token) : EnterAddressValidator {
    private val sendAdapter by lazy { App.adapterManager.getAdapterForToken<ISendStellarAdapter>(token) }
    override suspend fun validate(address: Address) {
        val adapter = sendAdapter ?: throw AddressValidationError.NoAdapter()
        adapter.validate(address.hex)
    }
}

class MoneroAddressValidator() : EnterAddressValidator {
    override suspend fun validate(address: Address) {
        MoneroKit.validateAddress(address.hex)
    }
}

class TronAddressValidator(
    private val token: Token,
    private val adapterManager: IAdapterManager
) : EnterAddressValidator {
    private val sendAdapter by lazy { adapterManager.getAdapterForToken<ISendTronAdapter>(token) }
    override suspend fun validate(address: Address) {
        val adapter = sendAdapter ?: throw AddressValidationError.NoAdapter()

        val validAddress = io.horizontalsystems.tronkit.models.Address.fromBase58(address.hex)
        if (token.type == TokenType.Native && adapter.isOwnAddress(validAddress)) {
            throw AddressValidationError.SendToSelfForbidden(
                Translator.getString(R.string.Send_Error_SendToSelf, "TRX")
            )
        }
    }
}

class ZcashAddressValidator(
    private val token: Token,
    private val adapterManager: IAdapterManager,
) : EnterAddressValidator {
    private val sendAdapter by lazy { adapterManager.getAdapterForToken<ISendZcashAdapter>(token) }

    override suspend fun validate(address: Address) {
        val adapter = sendAdapter ?: throw AddressValidationError.NoAdapter()

        try {
            adapter.validate(address.hex)
        } catch (e: ZcashError.SendToSelfNotAllowed) {
            throw AddressValidationError.SendToSelfForbidden(
                Translator.getString(R.string.Send_Error_SendToSelf, "ZEC")
            )
        }
    }
}

sealed class AddressValidationError : Throwable() {
    class NoAdapter : AddressValidationError() {
        override val message = "Send adapter is not found"
    }
    class SendToSelfForbidden(override val message: String) : AddressValidationError()
}