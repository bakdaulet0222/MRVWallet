package com.mrv.wallet.modules.pin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrv.wallet.R
import com.mrv.wallet.modules.pin.set.PinSetModule
import com.mrv.wallet.modules.pin.set.PinSetViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.animations.CrossSlide
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.HsBackButton

@Composable
fun PinSet(
    title: String,
    description: String,
    dismissWithSuccess: () -> Unit,
    onBackPress: () -> Unit,
    forDuress: Boolean = false,
    viewModel: PinSetViewModel = viewModel(factory = PinSetModule.Factory(forDuress))
) {
    if (viewModel.uiState.finished) {
        dismissWithSuccess.invoke()
        viewModel.finished()
    }

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            AppBar(
                title = title,
                navigationIcon = {
                    HsBackButton(onClick = onBackPress)
                }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = ComposeAppTheme.colors.tyler)
            ) {
                CrossSlide(
                    targetState = viewModel.uiState.stage,
                    modifier = Modifier.weight(1f),
                    reverseAnimation = viewModel.uiState.reverseSlideAnimation
                ) { stage ->
                    when (stage) {
                        PinSetModule.SetStage.Enter -> {
                            PinTopBlock(
                                title = description,
                                error = viewModel.uiState.error,
                                enteredCount = viewModel.uiState.enteredCount,
                            )
                        }
                        PinSetModule.SetStage.Confirm -> {
                            PinTopBlock(
                                title = stringResource(R.string.PinSet_ConfirmInfo),
                                enteredCount = viewModel.uiState.enteredCount,
                            )
                        }
                    }
                }

                PinNumpad(
                    onNumberClick = { number -> viewModel.onKeyClick(number) },
                    onDeleteClick = { viewModel.onDelete() },
                )
            }
        }
    }
}
