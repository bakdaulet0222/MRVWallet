package com.mrv.wallet.modules.settings.subscription

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.slideFromBottom
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.StatPremiumTrigger
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.body_jacob
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.cell.CellUniversal
import com.mrv.wallet.ui.compose.components.cell.SectionUniversalLawrence

class SubscriptionFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        SubscriptionScreen(navController)
    }

}

@Composable
fun SubscriptionScreen(navController: NavController) {
    val viewModel = viewModel<SubscriptionViewModel>()

    val uiState = viewModel.uiState
    val context = LocalContext.current

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Settings_Subscription),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            VSpacer(12.dp)

            SectionUniversalLawrence {
                if (uiState.userHasActiveSubscription) {
                    CellUniversal(
                        borderTop = false,
                        onClick = {
                            viewModel.launchManageSubscriptionScreen(context)
                        }
                    ) {
                        body_leah(
                            text = stringResource(R.string.SettingsSubscription_ManageSubscription),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = null,
                        )
                    }
                } else {
                    CellUniversal(
                        borderTop = false,
                        onClick = {
                            navController.slideFromBottom(R.id.buySubscriptionFragment)
                            stat(
                                page = StatPage.PurchaseList,
                                event = StatEvent.OpenPremium(StatPremiumTrigger.GetPremium)
                            )
                        }
                    ) {
                        body_leah(
                            text = stringResource(R.string.SettingsSubscription_GetPremium),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = null,
                        )
                    }
                    CellUniversal(
                        onClick = viewModel::restorePurchase
                    ) {
                        body_jacob(
                            text = stringResource(R.string.SettingsSubscription_RestorePurchase),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}
