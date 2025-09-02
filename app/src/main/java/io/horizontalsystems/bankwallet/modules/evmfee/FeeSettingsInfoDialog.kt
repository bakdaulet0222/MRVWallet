package com.mrv.wallet.modules.evmfee

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mrv.wallet.R
import com.mrv.wallet.core.requireInput
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.InfoTextBody
import com.mrv.wallet.ui.extensions.BaseComposableBottomSheetFragment
import com.mrv.wallet.ui.extensions.BottomSheetHeader
import io.horizontalsystems.core.findNavController
import kotlinx.parcelize.Parcelize

class FeeSettingsInfoDialog : BaseComposableBottomSheetFragment() {

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
                val input = navController.requireInput<Input>()

                ComposeAppTheme {
                    FeeSettingsInfoScreen(input.title, input.text) { dismiss() }
                }
            }
        }
    }

    @Parcelize
    data class Input(val title: String, val text: String) : Parcelable
}

@Composable
fun FeeSettingsInfoScreen(title: String?, text: String?, onCloseClick: () -> Unit) {
    BottomSheetHeader(
        iconPainter = painterResource(R.drawable.ic_info_24),
        iconTint = ColorFilter.tint(ComposeAppTheme.colors.grey),
        title = title ?: "",
        onCloseClick = onCloseClick
    ) {
        InfoTextBody(text = text ?: "")
        Spacer(modifier = Modifier.height(52.dp))
    }
}
