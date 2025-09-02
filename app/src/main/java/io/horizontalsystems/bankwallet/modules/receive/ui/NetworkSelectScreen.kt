package com.mrv.wallet.modules.receive.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mrv.wallet.R
import com.mrv.wallet.core.description
import com.mrv.wallet.core.imageUrl
import com.mrv.wallet.entities.Account
import com.mrv.wallet.entities.Wallet
import com.mrv.wallet.modules.receive.viewmodels.NetworkSelectViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.HsDivider
import com.mrv.wallet.ui.compose.components.InfoText
import com.mrv.wallet.ui.compose.components.RowUniversal
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.headline2_leah
import com.mrv.wallet.ui.compose.components.subhead2_grey
import io.horizontalsystems.marketkit.models.FullCoin
import kotlinx.coroutines.launch

@Composable
fun NetworkSelectScreen(
    navController: NavController,
    activeAccount: Account,
    fullCoin: FullCoin,
    onSelect: (Wallet) -> Unit
) {
    val viewModel = viewModel<NetworkSelectViewModel>(
        factory = NetworkSelectViewModel.Factory(
            activeAccount,
            fullCoin
        )
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Balance_Network),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
                menuItems = listOf()
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(ComposeAppTheme.colors.lawrence)
                .verticalScroll(rememberScrollState())
        ) {
            InfoText(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComposeAppTheme.colors.tyler),
                text = stringResource(R.string.Balance_NetworkSelectDescription),
                paddingBottom = 32.dp
            )
            viewModel.eligibleTokens.forEach { token ->
                val blockchain = token.blockchain
                NetworkCell(
                    title = blockchain.name,
                    subtitle = blockchain.description,
                    imageUrl = blockchain.type.imageUrl,
                    onClick = {
                        coroutineScope.launch {
                            onSelect.invoke(viewModel.getOrCreateWallet(token))
                        }
                    }
                )
                HsDivider()
            }
            VSpacer(32.dp)
        }
    }
}

@Composable
fun NetworkCell(
    title: String,
    subtitle: String,
    imageUrl: String,
    onClick: (() -> Unit)? = null
) {
    RowUniversal(
        onClick = onClick
    ) {
        Image(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(32.dp),
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                error = painterResource(R.drawable.ic_platform_placeholder_32)
            ),
            contentDescription = null,
        )
        Column(modifier = Modifier.weight(1f)) {
            headline2_leah(text = title)
            subhead2_grey(text = subtitle)
        }
        Icon(
            modifier = Modifier.padding(horizontal = 16.dp),
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = ComposeAppTheme.colors.grey
        )
    }
}
