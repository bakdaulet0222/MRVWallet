package com.mrv.wallet.modules.manageaccount.evmprivatekey

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.stats.StatEntity
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.modules.manageaccount.SecretKeyScreen
import kotlinx.parcelize.Parcelize

class EvmPrivateKeyFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Input>(navController) { input ->
            EvmPrivateKeyScreen(navController, input.evmPrivateKey)
        }
    }

    @Parcelize
    data class Input(val evmPrivateKey: String) : Parcelable
}

@Composable
fun EvmPrivateKeyScreen(
    navController: NavController,
    evmPrivateKey: String,
) {
    SecretKeyScreen(
        navController = navController,
        secretKey = evmPrivateKey,
        title = stringResource(R.string.EvmPrivateKey_Title),
        hideScreenText = stringResource(R.string.EvmPrivateKey_ShowPrivateKey),
        onCopyKey = {
            stat(
                page = StatPage.EvmPrivateKey,
                event = StatEvent.Copy(StatEntity.EvmPrivateKey)
            )
        },
        onOpenFaq = {
            stat(
                page = StatPage.EvmPrivateKey,
                event = StatEvent.Open(StatPage.Info)
            )
        },
        onToggleHidden = {
            stat(page = StatPage.EvmPrivateKey, event = StatEvent.ToggleHidden)
        }
    )
}
