package com.mrv.wallet.modules.send

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrv.wallet.R
import com.mrv.wallet.core.getInput
import com.mrv.wallet.core.setNavigationResultX
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.ButtonPrimaryRed
import com.mrv.wallet.ui.compose.components.ButtonPrimaryTransparent
import com.mrv.wallet.ui.compose.components.TextImportantError
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.extensions.BaseComposableBottomSheetFragment
import com.mrv.wallet.ui.extensions.BottomSheetHeader
import io.horizontalsystems.core.findNavController
import kotlinx.parcelize.Parcelize

class AddressRiskyBottomSheetAlert : BaseComposableBottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                val navController = findNavController()
                navController.getInput<Input>()?.let { input ->
                    RiskyAddressAlertView(
                        alertText = input.alertText,
                        onCloseClick = {
                            navController.popBackStack()
                        },
                        onContinueClick = {
                            navController.setNavigationResultX(Result(true))
                        }
                    )
                }
            }
        }
    }

    @Parcelize
    data class Input(val alertText: String) : Parcelable

    @Parcelize
    data class Result(val canContinue: Boolean) : Parcelable
}

@Composable
private fun RiskyAddressAlertView(
    alertText: String,
    onCloseClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    ComposeAppTheme {
        BottomSheetHeader(
            iconPainter = painterResource(R.drawable.ic_attention_24),
            iconTint = ColorFilter.tint(ComposeAppTheme.colors.lucian),
            title = stringResource(R.string.Send_RiskyAddress),
            onCloseClick = onCloseClick
        ) {
            VSpacer(12.dp)
            TextImportantError(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = alertText
            )
            VSpacer(32.dp)
            ButtonPrimaryRed(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                title = stringResource(R.string.Button_ContinueAnyway),
                onClick = {
                    onContinueClick()
                }
            )
            VSpacer(12.dp)
            ButtonPrimaryTransparent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                title = stringResource(R.string.Button_Cancel),
                onClick = onCloseClick
            )
            VSpacer(32.dp)
        }
    }
}
