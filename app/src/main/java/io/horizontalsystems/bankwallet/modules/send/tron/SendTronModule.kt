package com.mrv.wallet.modules.send.tron

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.core.ISendTronAdapter
import com.mrv.wallet.core.isNative
import com.mrv.wallet.entities.Address
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.amount.AmountValidator
import com.mrv.wallet.modules.amount.SendAmountService
import com.mrv.wallet.modules.xrate.XRateService
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import java.math.RoundingMode

object SendTronModule {

    class Factory(
        private val wallet: Wallet,
        private val address: Address,
        private val hideAddress: Boolean,
    ) : ViewModelProvider.Factory {
        val adapter = App.adapterManager.getAdapterForWallet<ISendTronAdapter>(wallet) ?: throw IllegalStateException("SendTronAdapter is null")

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                SendTronViewModel::class.java -> {
                    val amountValidator = AmountValidator()
                    val coinMaxAllowedDecimals = wallet.token.decimals

                    val amountService = SendAmountService(
                        amountValidator,
                        wallet.token.coin.code,
                        adapter.balanceData.available.setScale(coinMaxAllowedDecimals, RoundingMode.DOWN),
                        wallet.token.type.isNative,
                    )
                    val addressService = SendTronAddressService(adapter, wallet.token)
                    val xRateService = XRateService(App.marketKit, App.currencyManager.baseCurrency)
                    val feeToken = App.coinManager.getToken(TokenQuery(BlockchainType.Tron, TokenType.Native)) ?: throw IllegalArgumentException()

                    SendTronViewModel(
                        wallet,
                        wallet.token,
                        feeToken,
                        adapter,
                        xRateService,
                        amountService,
                        addressService,
                        coinMaxAllowedDecimals,
                        App.contactsRepository,
                        !hideAddress,
                        App.connectivityManager,
                        address,
                        App.recentAddressManager
                    ) as T
                }

                else -> throw IllegalArgumentException()
            }
        }
    }
}
