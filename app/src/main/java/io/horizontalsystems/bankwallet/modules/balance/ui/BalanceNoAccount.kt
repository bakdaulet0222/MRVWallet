package com.mrv.wallet.modules.balance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.navigateWithTermsAccepted
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.ButtonPrimaryDefault
import com.mrv.wallet.ui.compose.components.ButtonPrimaryOrange
import com.mrv.wallet.ui.compose.components.ButtonPrimaryTransparent
import com.mrv.wallet.ui.compose.components.VSpacer

@Composable
fun BalanceNoAccount(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(64.dp),
                painter = painterResource(R.drawable.ic_warning_64),
                contentDescription = "",
                tint = ComposeAppTheme.colors.grey
            )
        }
        VSpacer(24.dp)
        ButtonPrimaryOrange(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            title = stringResource(R.string.ManageAccounts_CreateNewWallet),
            onClick = {
                navController.navigateWithTermsAccepted {
                    navController.slideFromRight(R.id.createAccountFragment)

                    stat(page = StatPage.Balance, event = StatEvent.Open(StatPage.NewWallet))
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ButtonPrimaryDefault(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            title = stringResource(R.string.ManageAccounts_ImportWallet),
            onClick = {
                navController.navigateWithTermsAccepted {
                    navController.slideFromRight(R.id.importWalletFragment)

                    stat(page = StatPage.Balance, event = StatEvent.Open(StatPage.ImportWallet))
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ButtonPrimaryTransparent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            title = stringResource(R.string.ManageAccounts_WatchAddress),
            onClick = {
                navController.slideFromRight(R.id.watchAddressFragment)

                stat(page = StatPage.Balance, event = StatEvent.Open(StatPage.WatchWallet))
            }
        )

    }
}
