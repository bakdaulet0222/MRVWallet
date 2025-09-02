package com.mrv.wallet.modules.manageaccount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.managers.FaqManager
import com.mrv.wallet.modules.manageaccount.ui.ActionButton
import com.mrv.wallet.modules.manageaccount.ui.ConfirmCopyBottomSheet
import com.mrv.wallet.modules.manageaccount.ui.HidableContent
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.TextImportantWarning
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.helpers.TextHelper
import io.horizontalsystems.core.helpers.HudHelper
import kotlinx.coroutines.launch

@Composable
fun SecretKeyScreen(
    navController: NavController,
    secretKey: String,
    title: String,
    hideScreenText: String,
    onCopyKey: () -> Unit,
    onOpenFaq: () -> Unit,
    onToggleHidden: () -> Unit,
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetBackgroundColor = ComposeAppTheme.colors.transparent,
        sheetContent = {
            ConfirmCopyBottomSheet(
                onConfirm = {
                    coroutineScope.launch {
                        TextHelper.copyText(secretKey)
                        HudHelper.showSuccessMessage(view, R.string.Hud_Text_Copied)
                        sheetState.hide()

                        onCopyKey()
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                }
            )
        }
    ) {
        Scaffold(
            backgroundColor = ComposeAppTheme.colors.tyler,
            topBar = {
                AppBar(
                    title = title,
                    navigationIcon = {
                        HsBackButton(onClick = navController::popBackStack)
                    },
                    menuItems = listOf(
                        MenuItem(
                            title = TranslatableString.ResString(R.string.Info_Title),
                            icon = R.drawable.ic_info_24,
                            onClick = {
                                FaqManager.showFaqPage(navController, FaqManager.faqPathPrivateKeys)

                                onOpenFaq()
                            }
                        )
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top
                ) {
                    VSpacer(12.dp)
                    TextImportantWarning(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.PrivateKeys_NeverShareWarning)
                    )
                    VSpacer(24.dp)
                    HidableContent(secretKey, hideScreenText, onToggleHidden)
                }

                ActionButton(R.string.Alert_Copy) {
                    coroutineScope.launch {
                        sheetState.show()
                    }
                }
            }
        }
    }
}