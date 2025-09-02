package com.mrv.wallet.modules.tonconnect

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.tonapps.wallet.data.tonconnect.entities.DAppRequestEntity
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.setNavigationResultX
import kotlinx.parcelize.Parcelize

class TonConnectNewFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        withInput<DAppRequestEntity>(navController) { input ->
            TonConnectNewScreen(
                navController = navController,
                requestEntity = input,
                onResult = { approved ->
                    navController.setNavigationResultX(Result(approved))
                    navController.popBackStack()
                },
            )
        }
    }

    @Parcelize
    data class Result(val approved: Boolean) : Parcelable
}
