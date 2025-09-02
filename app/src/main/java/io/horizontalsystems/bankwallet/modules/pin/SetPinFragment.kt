package com.mrv.wallet.modules.pin

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.getInput
import com.mrv.wallet.core.setNavigationResultX
import com.mrv.wallet.modules.pin.ui.PinSet
import kotlinx.parcelize.Parcelize

class SetPinFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()

        PinSet(
            title = stringResource(R.string.PinSet_Title),
            description = stringResource(input?.descriptionResId ?: R.string.PinSet_Info),
            dismissWithSuccess = {
                navController.setNavigationResultX(Result(true))
                navController.popBackStack()
            },
            onBackPress = { navController.popBackStack() }
        )
    }

    @Parcelize
    data class Input(val descriptionResId: Int) : Parcelable

    @Parcelize
    data class Result(val success: Boolean) : Parcelable
}
