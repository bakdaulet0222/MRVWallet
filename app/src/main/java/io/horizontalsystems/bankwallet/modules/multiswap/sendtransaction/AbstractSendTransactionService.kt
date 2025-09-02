package com.mrv.wallet.modules.multiswap.sendtransaction

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mrv.wallet.core.App
import com.mrv.wallet.core.ServiceState
import com.mrv.wallet.core.ethereum.CautionViewItem
import com.mrv.wallet.entities.CoinValue
import com.mrv.wallet.entities.CurrencyValue
import com.mrv.wallet.modules.multiswap.ui.DataField
import com.mrv.wallet.modules.send.SendModule
import io.horizontalsystems.marketkit.models.Coin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

abstract class AbstractSendTransactionService(val hasSettings: Boolean): ServiceState<SendTransactionServiceState>() {
    open val mevProtectionAvailable: Boolean = false
    abstract val sendTransactionSettingsFlow: StateFlow<SendTransactionSettings>
    protected var uuid = UUID.randomUUID().toString()
    protected var extraFees = mapOf<FeeType, SendModule.AmountData>()

    private val baseCurrency = App.currencyManager.baseCurrency

    abstract fun start(coroutineScope: CoroutineScope)
    abstract suspend fun setSendTransactionData(data: SendTransactionData)
    @Composable
    open fun GetSettingsContent(navController: NavController) = Unit
    abstract suspend fun sendTransaction(mevProtectionEnabled: Boolean = false): SendTransactionResult

    fun refreshUuid() {
        uuid = UUID.randomUUID().toString()
    }

    protected fun setExtraFeesMap(feesMap: Map<FeeType, CoinValue>) {
        extraFees = feesMap.mapValues { (_, coinValue) ->
            getAmountData(coinValue)
        }
    }

    protected fun getAmountData(coinValue: CoinValue): SendModule.AmountData {
        val primaryAmountInfo = SendModule.AmountInfo.CoinValueInfo(coinValue)

        val secondaryAmountInfo = getRate(coinValue.coin)?.let { rate ->
            SendModule.AmountInfo.CurrencyValueInfo(CurrencyValue(rate.currency, rate.value * coinValue.value))
        }
        return SendModule.AmountData(primaryAmountInfo, secondaryAmountInfo)
    }

    private fun getRate(coin: Coin): CurrencyValue? {
        return App.marketKit.coinPrice(coin.uid, baseCurrency.code)?.let {
            CurrencyValue(baseCurrency, it.value)
        }
    }
}

data class SendTransactionServiceState(
    val uuid: String,
    val networkFee: SendModule.AmountData?,
    val cautions: List<CautionViewItem>,
    val sendable: Boolean,
    val loading: Boolean,
    val fields: List<DataField>,
    val extraFees: Map<FeeType, SendModule.AmountData>
)
