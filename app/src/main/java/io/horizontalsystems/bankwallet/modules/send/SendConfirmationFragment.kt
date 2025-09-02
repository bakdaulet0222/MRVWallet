package com.mrv.wallet.modules.send

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.modules.amount.AmountInputModeViewModel
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinConfirmationScreen
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinViewModel
import com.mrv.wallet.modules.send.monero.SendMoneroConfirmationScreen
import com.mrv.wallet.modules.send.monero.SendMoneroViewModel
import com.mrv.wallet.modules.send.solana.SendSolanaConfirmationScreen
import com.mrv.wallet.modules.send.solana.SendSolanaViewModel
import com.mrv.wallet.modules.send.stellar.SendStellarConfirmationScreen
import com.mrv.wallet.modules.send.stellar.SendStellarViewModel
import com.mrv.wallet.modules.send.ton.SendTonConfirmationScreen
import com.mrv.wallet.modules.send.ton.SendTonViewModel
import com.mrv.wallet.modules.send.tron.SendTronConfirmationScreen
import com.mrv.wallet.modules.send.tron.SendTronViewModel
import com.mrv.wallet.modules.send.zcash.SendZCashConfirmationScreen
import com.mrv.wallet.modules.send.zcash.SendZCashViewModel
import kotlinx.parcelize.Parcelize

class SendConfirmationFragment : BaseComposeFragment() {
    val amountInputModeViewModel by navGraphViewModels<AmountInputModeViewModel>(R.id.sendXFragment)

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Input>(navController) { input ->
            when (input.type) {
                Type.Bitcoin -> {
                    val sendBitcoinViewModel by navGraphViewModels<SendBitcoinViewModel>(R.id.sendXFragment)

                    SendBitcoinConfirmationScreen(
                        navController,
                        sendBitcoinViewModel,
                        amountInputModeViewModel,
                        input.sendEntryPointDestId
                    )
                }

                Type.ZCash -> {
                    val sendZCashViewModel by navGraphViewModels<SendZCashViewModel>(R.id.sendXFragment)

                    SendZCashConfirmationScreen(
                        navController,
                        sendZCashViewModel,
                        amountInputModeViewModel,
                        input.sendEntryPointDestId
                    )
                }

                Type.Tron -> {
                    val sendTronViewModel by navGraphViewModels<SendTronViewModel>(R.id.sendXFragment)
                    SendTronConfirmationScreen(
                        navController,
                        sendTronViewModel,
                        amountInputModeViewModel,
                        input.sendEntryPointDestId
                    )
                }

                Type.Solana -> {
                    val sendSolanaViewModel by navGraphViewModels<SendSolanaViewModel>(R.id.sendXFragment)

                    SendSolanaConfirmationScreen(
                        navController,
                        sendSolanaViewModel,
                        amountInputModeViewModel,
                        input.sendEntryPointDestId
                    )
                }

                Type.Ton -> {
                    val sendTonViewModel by navGraphViewModels<SendTonViewModel>(R.id.sendXFragment)

                    SendTonConfirmationScreen(
                        navController,
                        sendTonViewModel,
                        amountInputModeViewModel,
                        input.sendEntryPointDestId
                    )
                }

                Type.Stellar -> {
                    val sendStellarViewModel by navGraphViewModels<SendStellarViewModel>(R.id.sendXFragment)

                    SendStellarConfirmationScreen(
                        navController,
                        sendStellarViewModel,
                        amountInputModeViewModel,
                        input.sendEntryPointDestId
                    )
                }

                Type.Monero -> {
                    val sendMoneroViewModel by navGraphViewModels<SendMoneroViewModel>(R.id.sendXFragment)

                    SendMoneroConfirmationScreen(
                        navController,
                        sendMoneroViewModel,
                        amountInputModeViewModel,
                        input.sendEntryPointDestId
                    )
                }
            }
        }
    }

    @Parcelize
    enum class Type : Parcelable {
        Bitcoin, ZCash, Solana, Tron, Ton, Stellar, Monero
    }

    @Parcelize
    data class Input(val type: Type, val sendEntryPointDestId: Int) : Parcelable
}
