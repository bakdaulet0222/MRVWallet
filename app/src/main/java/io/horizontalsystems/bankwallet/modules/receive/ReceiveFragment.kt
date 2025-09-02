package com.mrv.wallet.modules.receive

import android.os.Parcelable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.receive.ui.ReceiveAddressScreen
import com.mrv.wallet.modules.receive.ui.UsedAddressesParams
import com.mrv.wallet.modules.receive.viewmodels.ReceiveAddressViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.HsDivider
import com.mrv.wallet.ui.compose.components.RowUniversal
import com.mrv.wallet.ui.compose.components.subhead2_grey
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.parcelize.Parcelize

class ReceiveFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Input>(navController) {
            val wallet = it.wallet
            val token = wallet.token
            when (token.blockchainType) {
                BlockchainType.Stellar -> {
                    if (token.type is TokenType.Asset) {
                        ReceiveStellarAssetScreen(navController, wallet, it.receiveEntryPointDestId)
                    } else if (token.type == TokenType.Native) {
                        ReceiveScreen(navController, wallet, it.receiveEntryPointDestId)
                    }
                }
//        BlockchainType.ArbitrumOne -> TODO()
//        BlockchainType.Avalanche -> TODO()
//        BlockchainType.Base -> TODO()
//        BlockchainType.BinanceSmartChain -> TODO()
//        BlockchainType.Bitcoin -> TODO()
//        BlockchainType.BitcoinCash -> TODO()
//        BlockchainType.Dash -> TODO()
//        BlockchainType.ECash -> TODO()
//        BlockchainType.Ethereum -> TODO()
//        BlockchainType.Fantom -> TODO()
//        BlockchainType.Gnosis -> TODO()
//        BlockchainType.Litecoin -> TODO()
//        BlockchainType.Optimism -> TODO()
//        BlockchainType.Polygon -> TODO()
//        BlockchainType.Solana -> TODO()
//        BlockchainType.Ton -> TODO()
//        BlockchainType.Tron -> TODO()
//        is BlockchainType.Unsupported -> TODO()
//        BlockchainType.Zcash -> TODO()
//        BlockchainType.ZkSync -> TODO()
                else -> {
                    ReceiveScreen(navController, wallet, it.receiveEntryPointDestId)
                }
            }
        }
    }

    @Parcelize
    data class Input(val wallet: Wallet, val receiveEntryPointDestId: Int = 0) : Parcelable

}

@Composable
fun ReceiveScreen(navController: NavController, wallet: Wallet, receiveEntryPointDestId: Int) {
    val addressViewModel = viewModel<ReceiveAddressViewModel>(factory = ReceiveModule.Factory(wallet))

    val uiState = addressViewModel.uiState
    ReceiveAddressScreen(
        title = stringResource(R.string.Deposit_Title, wallet.coin.code),
        uiState = uiState,
        setAmount = { amount -> addressViewModel.setAmount(amount) },
        onErrorClick = { addressViewModel.onErrorClick() },
        slot1 = {
            if (uiState.usedAddresses.isNotEmpty()) {
                HsDivider(modifier = Modifier.fillMaxWidth())
                RowUniversal(
                    modifier = Modifier.height(52.dp),
                    onClick = {
                        navController.slideFromRight(
                            R.id.btcUsedAddressesFragment,
                            UsedAddressesParams(
                                wallet.coin.name,
                                uiState.usedAddresses,
                                uiState.usedChangeAddresses
                            )
                        )
                    }
                ) {
                    subhead2_grey(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                        text = stringResource(R.string.Balance_Receive_UsedAddresses),
                    )

                    Icon(
                        modifier = Modifier.padding(end = 16.dp),
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = ComposeAppTheme.colors.grey
                    )
                }
            }
        },
        onBackPress = { navController.popBackStack() },
        closeModule = {
            if (receiveEntryPointDestId == 0) {
                navController.popBackStack()
            } else {
                navController.popBackStack(receiveEntryPointDestId, true)
            }
        }
    )
}
