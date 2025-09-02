package com.mrv.wallet.modules.manageaccount.stellarsecretkey

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

class StellarSecretKeyFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Input>(navController) { input ->
            StellarSecretKeyScreen(navController, input.stellarSecretKey)
        }
    }

    @Parcelize
    data class Input(val stellarSecretKey: String) : Parcelable
}

@Composable
fun StellarSecretKeyScreen(
    navController: NavController,
    stellarSecretKey: String,
) {
    SecretKeyScreen(
        navController = navController,
        secretKey = stellarSecretKey,
        title = stringResource(R.string.StellarSecretKey_Title),
        hideScreenText = stringResource(R.string.StellarSecretKey_ShowSecretKey),
        onCopyKey = {
            stat(
                page = StatPage.StellarSecretKey,
                event = StatEvent.Copy(StatEntity.StellarSecretKey)
            )
        },
        onOpenFaq = {
            stat(
                page = StatPage.StellarSecretKey,
                event = StatEvent.Open(StatPage.Info)
            )
        },
        onToggleHidden = {
            stat(page = StatPage.StellarSecretKey, event = StatEvent.ToggleHidden)
        }
    )
}
