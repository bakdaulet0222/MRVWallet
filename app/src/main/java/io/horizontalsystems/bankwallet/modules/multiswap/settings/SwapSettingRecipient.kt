package com.mrv.wallet.modules.multiswap.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mrv.wallet.entities.Address
import com.mrv.wallet.modules.multiswap.settings.ui.RecipientAddress
import io.horizontalsystems.marketkit.models.Token

data class SwapSettingRecipient(
    val settings: Map<String, Any?>,
    val tokenOut: Token
) : ISwapSetting {
    override val id = "recipient_${tokenOut.blockchainType.uid}"

    val value = settings[id] as? Address

    @Composable
    override fun GetContent(
        navController: NavController,
        onError: (Throwable?) -> Unit,
        onValueChange: (Any?) -> Unit
    ) {
        RecipientAddress(
            token = tokenOut,
            navController = navController,
            initial = value,
            onError = onError,
            onValueChange = onValueChange
        )
    }

    fun getEthereumKitAddress(): io.horizontalsystems.ethereumkit.models.Address? {
        val hex = value?.hex ?: return null

        return try {
            io.horizontalsystems.ethereumkit.models.Address(hex)
        } catch (err: Exception) {
            null
        }
    }
}
