package com.mrv.wallet.modules.multiswap.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.modules.evmfee.ButtonsGroupWithShade
import com.mrv.wallet.modules.multiswap.SwapViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.ButtonPrimaryYellow
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.VSpacer

class SwapSettingsFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        SwapProviderSettingsScreen(navController)
    }
}

@Composable
private fun SwapProviderSettingsScreen(navController: NavController) {
    val previousBackStackEntry = remember { navController.previousBackStackEntry }
    val swapViewModel = viewModel<SwapViewModel>(
        viewModelStoreOwner = previousBackStackEntry!!,
    )

    val viewModel = viewModel<SwapSettingsViewModel>(factory = SwapSettingsViewModel.Factory(swapViewModel.getSettings()))

    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.SwapSettings_Title),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                ButtonPrimaryYellow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    title = stringResource(id = R.string.SwapSettings_Apply),
                    enabled = uiState.applyEnabled,
                    onClick = {
                        swapViewModel.onUpdateSettings(viewModel.getSettings())
                        navController.popBackStack()
                    }
                )
            }
        },
        backgroundColor = ComposeAppTheme.colors.tyler,
    ) {
        LazyColumn(
            modifier = Modifier.padding(it),
        ) {
            item {
                VSpacer(height = 12.dp)
            }

            swapViewModel.uiState.quote?.swapQuote?.let { swapQuote ->
                items(swapQuote.settings) { setting ->
                    val settingId = setting.id

                    setting.GetContent(
                        navController = navController,
                        onError = {
                            viewModel.onSettingError(settingId, it)
                        },
                        onValueChange = {
                            viewModel.onSettingEnter(settingId, it)
                        }
                    )
                }
            }

            item {
            }
        }
    }
}
