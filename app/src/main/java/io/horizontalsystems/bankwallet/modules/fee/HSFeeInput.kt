package com.mrv.wallet.modules.fee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.entities.CurrencyValue
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.amount.AmountInputType
import com.mrv.wallet.ui.compose.components.CellUniversalLawrenceSection
import java.math.BigDecimal

@Composable
fun HSFee(
    coinCode: String,
    coinDecimal: Int,
    fee: BigDecimal?,
    amountInputType: AmountInputType,
    rate: CurrencyValue?,
    navController: NavController,
    viewState: ViewState? = null
) {
    CellUniversalLawrenceSection(
        listOf {
            HSFeeRaw(
                coinCode = coinCode,
                coinDecimal = coinDecimal,
                fee = fee,
                amountInputType = amountInputType,
                rate = rate,
                navController = navController,
                viewState = viewState
            )
        })
}

@Composable
fun HSFeeRaw(
    title: String = stringResource(R.string.Send_Fee),
    info: String = stringResource(R.string.Send_Fee_Info),
    coinCode: String,
    coinDecimal: Int,
    fee: BigDecimal?,
    amountInputType: AmountInputType,
    rate: CurrencyValue?,
    navController: NavController,
    viewState: ViewState? = null
) {

    var formatted by remember { mutableStateOf<FeeItem?>(null) }

    LaunchedEffect(fee, amountInputType, rate) {
        formatted = getFormatted(fee, rate, coinCode, coinDecimal, amountInputType)
    }

    FeeCell(
        title = title,
        info = info,
        value = formatted,
        viewState = viewState,
        navController = navController
    )
}

@Composable
fun HSFeeRawWithViewState(
    title: String = stringResource(R.string.Send_Fee),
    info: String = stringResource(R.string.Send_Fee_Info),
    coinCode: String,
    coinDecimal: Int,
    fee: BigDecimal?,
    viewState: ViewState,
    amountInputType: AmountInputType,
    rate: CurrencyValue?,
    navController: NavController
) {
    var formatted by remember { mutableStateOf<FeeItem?>(null) }

    LaunchedEffect(fee, amountInputType, rate) {
        formatted = getFormatted(fee, rate, coinCode, coinDecimal, amountInputType)
    }

    FeeCell(
        title = title,
        info = info,
        value = formatted,
        viewState = viewState,
        navController = navController
    )
}

private fun getFormatted(
    fee: BigDecimal?,
    rate: CurrencyValue?,
    coinCode: String,
    coinDecimal: Int,
    amountInputType: AmountInputType
): FeeItem? {

    if (fee == null) return null

    val coinAmount = App.numberFormatter.formatCoinFull(fee, coinCode, coinDecimal)
    val currencyAmount = rate?.let {
        it.copy(value = fee.times(it.value)).getFormattedFull()
    }

    return if (amountInputType == AmountInputType.CURRENCY && currencyAmount != null) {
        FeeItem(currencyAmount, coinAmount)
    } else {
        FeeItem(coinAmount, currencyAmount)
    }
}