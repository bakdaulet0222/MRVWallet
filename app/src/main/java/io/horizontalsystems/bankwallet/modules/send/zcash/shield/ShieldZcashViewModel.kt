package com.mrv.wallet.modules.send.zcash.shield

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.sdk.ext.collectWith
import com.mrv.wallet.R
import com.mrv.wallet.core.AppLogger
import com.mrv.wallet.core.HSCaution
import com.mrv.wallet.core.LocalizedException
import com.mrv.wallet.core.adapters.zcash.ZcashAdapter
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.send.SendConfirmationData
import com.mrv.wallet.modules.send.SendResult
import com.mrv.wallet.modules.xrate.XRateService
import com.mrv.wallet.ui.compose.TranslatableString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.net.UnknownHostException

class ShieldZcashViewModel(
    private val adapter: ZcashAdapter,
    private val wallet: Wallet,
    private val xRateService: XRateService,
) : ViewModel() {
    private val logger = AppLogger("Shield-Zcash")

    val blockchainType = wallet.token.blockchainType
    val coinMaxAllowedDecimals = wallet.token.decimals

    var coinRate by mutableStateOf(xRateService.getRate(wallet.coin.uid))
        private set

    var sendResult by mutableStateOf<SendResult?>(null)
        private set

    var fee by mutableStateOf<BigDecimal?>(null)
        private set

    init {
        xRateService.getRateFlow(wallet.coin.uid).collectWith(viewModelScope) {
            coinRate = it
        }

        viewModelScope.launch {
            fee = adapter.shieldTransactionFee()
        }
    }

    fun getConfirmationData(): SendConfirmationData {
        return SendConfirmationData(
            amount = adapter.balanceData.unshielded,
            fee = null,
            address = null,
            contact = null,
            coin = wallet.coin,
            feeCoin = wallet.coin,
            memo = null
        )
    }

    fun onClickSend() {
        viewModelScope.launch {
            send()
        }
    }

    private suspend fun send() = withContext(Dispatchers.IO) {
        val logger = logger.getScopedUnique()
        logger.info("click")

        try {
            sendResult = SendResult.Sending

            adapter.sendShieldProposal()

            logger.info("success")
            sendResult = SendResult.Sent()

        } catch (e: Throwable) {
            logger.warning("failed", e)
            sendResult = SendResult.Failed(createCaution(e))
        }
    }

    private fun createCaution(error: Throwable) = when (error) {
        is UnknownHostException -> HSCaution(TranslatableString.ResString(R.string.Hud_Text_NoInternet))
        is LocalizedException -> HSCaution(TranslatableString.ResString(error.errorTextRes))
        else -> HSCaution(TranslatableString.PlainString(error.message ?: ""))
    }
}
