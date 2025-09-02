package com.mrv.wallet.modules.settings.security

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.authorizedAction
import com.mrv.wallet.core.ensurePinSet
import com.mrv.wallet.core.paidAction
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatPremiumTrigger
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.modules.settings.security.passcode.SecuritySettingsViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.PremiumHeader
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.body_lucian
import com.mrv.wallet.ui.compose.components.cell.SectionPremiumUniversalLawrence
import io.horizontalsystems.subscriptions.core.DuressMode

@Composable
fun DuressPasscodeBlock(
    viewModel: SecuritySettingsViewModel,
    navController: NavController
) {
    val uiState = viewModel.uiState
    PremiumHeader()
    SectionPremiumUniversalLawrence {
        SecurityCenterCell(
            start = {
                Icon(
                    painter = painterResource(R.drawable.ic_switch_wallet_24),
                    tint = ComposeAppTheme.colors.jacob,
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                )
            },
            center = {
                val text = if (uiState.duressPinEnabled) {
                    R.string.SettingsSecurity_EditDuressPin
                } else {
                    R.string.SettingsSecurity_SetDuressPin
                }
                body_leah(
                    text = stringResource(text),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            onClick = {
                navController.paidAction(DuressMode) {
                    if (uiState.pinEnabled) {
                        navController.authorizedAction {
                            if (uiState.duressPinEnabled) {
                                navController.slideFromRight(R.id.editDuressPinFragment)
                            } else {
                                navController.slideFromRight(R.id.setDuressPinIntroFragment)
                            }
                        }
                    } else {
                        navController.ensurePinSet(R.string.PinSet_ForDuress) {
                            navController.slideFromRight(R.id.setDuressPinIntroFragment)
                        }
                    }
                }
                stat(
                    page = StatPage.Security,
                    event = StatEvent.OpenPremium(StatPremiumTrigger.DuressMode)
                )
            }
        )
        if (uiState.duressPinEnabled) {
            SecurityCenterCell(
                start = {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_20),
                        tint = ComposeAppTheme.colors.lucian,
                        modifier = Modifier.size(24.dp),
                        contentDescription = null,
                    )
                },
                center = {
                    body_lucian(
                        text = stringResource(R.string.SettingsSecurity_DisableDuressPin),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                onClick = {
                    navController.authorizedAction {
                        viewModel.disableDuressPin()
                    }
                }
            )
        }
    }
}
