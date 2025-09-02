package com.mrv.wallet.modules.send

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.navGraphViewModels
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseFragment
import com.mrv.wallet.core.requireInput
import com.mrv.wallet.entities.Address
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.amount.AmountInputModeModule
import com.mrv.wallet.modules.amount.AmountInputModeViewModel
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinModule
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinNavHost
import com.mrv.wallet.modules.send.bitcoin.SendBitcoinViewModel
import com.mrv.wallet.modules.send.evm.SendEvmScreen
import com.mrv.wallet.modules.send.monero.SendMoneroModule
import com.mrv.wallet.modules.send.monero.SendMoneroScreen
import com.mrv.wallet.modules.send.monero.SendMoneroViewModel
import com.mrv.wallet.modules.send.solana.SendSolanaModule
import com.mrv.wallet.modules.send.solana.SendSolanaScreen
import com.mrv.wallet.modules.send.solana.SendSolanaViewModel
import com.mrv.wallet.modules.send.stellar.SendStellarModule
import com.mrv.wallet.modules.send.stellar.SendStellarScreen
import com.mrv.wallet.modules.send.stellar.SendStellarViewModel
import com.mrv.wallet.modules.send.ton.SendTonModule
import com.mrv.wallet.modules.send.ton.SendTonScreen
import com.mrv.wallet.modules.send.ton.SendTonViewModel
import com.mrv.wallet.modules.send.tron.SendTronModule
import com.mrv.wallet.modules.send.tron.SendTronScreen
import com.mrv.wallet.modules.send.tron.SendTronViewModel
import com.mrv.wallet.modules.send.zcash.SendZCashModule
import com.mrv.wallet.modules.send.zcash.SendZCashScreen
import com.mrv.wallet.modules.send.zcash.SendZCashViewModel
import io.horizontalsystems.core.findNavController
import io.horizontalsystems.marketkit.models.BlockchainType
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

class SendFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            try {
                val navController = findNavController()
                val input = navController.requireInput<Input>()
                val wallet = input.wallet
                val title = input.title
                val sendEntryPointDestId = input.sendEntryPointDestId
                val address = input.address
                val riskyAddress = input.riskyAddress
                val hideAddress = input.hideAddress
                val amount = input.amount

                val amountInputModeViewModel by navGraphViewModels<AmountInputModeViewModel>(R.id.sendXFragment) {
                    AmountInputModeModule.Factory(wallet.coin.uid)
                }

                when (wallet.token.blockchainType) {
                    BlockchainType.Bitcoin,
                    BlockchainType.BitcoinCash,
                    BlockchainType.ECash,
                    BlockchainType.Litecoin,
                    BlockchainType.Dash -> {
                        val factory = SendBitcoinModule.Factory(wallet, address, hideAddress)
                        val sendBitcoinViewModel by navGraphViewModels<SendBitcoinViewModel>(R.id.sendXFragment) {
                            factory
                        }
                        setContent {
                            SendBitcoinNavHost(
                                title = title,
                                fragmentNavController = findNavController(),
                                viewModel = sendBitcoinViewModel,
                                amountInputModeViewModel = amountInputModeViewModel,
                                sendEntryPointDestId = sendEntryPointDestId,
                                amount = amount,
                                riskyAddress = riskyAddress
                            )
                        }
                    }

                    BlockchainType.Zcash -> {
                        val factory = SendZCashModule.Factory(wallet, address, hideAddress)
                        val sendZCashViewModel by navGraphViewModels<SendZCashViewModel>(R.id.sendXFragment) {
                            factory
                        }
                        setContent {
                            SendZCashScreen(
                                title = title,
                                navController = findNavController(),
                                viewModel = sendZCashViewModel,
                                amountInputModeViewModel = amountInputModeViewModel,
                                sendEntryPointDestId = sendEntryPointDestId,
                                amount = amount,
                                riskyAddress = riskyAddress
                            )
                        }
                    }

                    BlockchainType.Ethereum,
                    BlockchainType.BinanceSmartChain,
                    BlockchainType.Polygon,
                    BlockchainType.Avalanche,
                    BlockchainType.Optimism,
                    BlockchainType.Base,
                    BlockchainType.ZkSync,
                    BlockchainType.Gnosis,
                    BlockchainType.Fantom,
                    BlockchainType.ArbitrumOne -> {
                        setContent {
                            SendEvmScreen(
                                title = title,
                                navController = findNavController(),
                                amountInputModeViewModel = amountInputModeViewModel,
                                address = address,
                                wallet = wallet,
                                amount = amount,
                                hideAddress = hideAddress,
                                riskyAddress = riskyAddress,
                                sendEntryPointDestId = sendEntryPointDestId
                            )
                        }
                    }

                    BlockchainType.Solana -> {
                        val factory = SendSolanaModule.Factory(wallet, address, hideAddress)
                        val sendSolanaViewModel by navGraphViewModels<SendSolanaViewModel>(R.id.sendXFragment) { factory }
                        setContent {
                            SendSolanaScreen(
                                title = title,
                                navController = findNavController(),
                                viewModel = sendSolanaViewModel,
                                amountInputModeViewModel = amountInputModeViewModel,
                                sendEntryPointDestId = sendEntryPointDestId,
                                amount = amount,
                                riskyAddress = riskyAddress
                            )
                        }
                    }

                    BlockchainType.Ton -> {
                        val factory = SendTonModule.Factory(wallet, address, hideAddress)
                        val sendTonViewModel by navGraphViewModels<SendTonViewModel>(R.id.sendXFragment) { factory }
                        setContent {
                            SendTonScreen(
                                title,
                                findNavController(),
                                sendTonViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                amount,
                                riskyAddress = riskyAddress
                            )
                        }
                    }

                    BlockchainType.Tron -> {
                        val factory = SendTronModule.Factory(wallet, address, hideAddress)
                        val sendTronViewModel by navGraphViewModels<SendTronViewModel>(R.id.sendXFragment) { factory }
                        setContent {
                            SendTronScreen(
                                title = title,
                                navController = findNavController(),
                                viewModel = sendTronViewModel,
                                amountInputModeViewModel = amountInputModeViewModel,
                                sendEntryPointDestId = sendEntryPointDestId,
                                amount = amount,
                                riskyAddress = riskyAddress
                            )
                        }
                    }

                    BlockchainType.Stellar -> {
                        val factory = SendStellarModule.Factory(wallet, address, hideAddress)
                        val sendStellarViewModel by navGraphViewModels<SendStellarViewModel>(R.id.sendXFragment) { factory }
                        setContent {
                            SendStellarScreen(
                                title,
                                findNavController(),
                                sendStellarViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                amount,
                                riskyAddress = riskyAddress
                            )
                        }
                    }

                    BlockchainType.Monero -> {
                        val factory = SendMoneroModule.Factory(wallet, address, hideAddress)
                        val sendMoneroViewModel by navGraphViewModels<SendMoneroViewModel>(R.id.sendXFragment) { factory }
                        setContent {
                            SendMoneroScreen(
                                title,
                                findNavController(),
                                sendMoneroViewModel,
                                amountInputModeViewModel,
                                sendEntryPointDestId,
                                amount,
                                riskyAddress = riskyAddress
                            )
                        }
                    }


                    else -> {}
                }
            } catch (t: Throwable) {
                findNavController().popBackStack()
            }
        }
    }

    @Parcelize
    data class Input(
        val wallet: Wallet,
        val title: String,
        val sendEntryPointDestId: Int,
        val address: Address,
        val riskyAddress: Boolean = false,
        val amount: BigDecimal? = null,
        val hideAddress: Boolean = false
    ) : Parcelable
}
