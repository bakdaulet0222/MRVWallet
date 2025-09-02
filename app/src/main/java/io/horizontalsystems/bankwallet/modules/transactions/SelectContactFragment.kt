package com.mrv.wallet.modules.transactions

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.getInput
import com.mrv.wallet.core.setNavigationResultX
import com.mrv.wallet.modules.contacts.model.Contact
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.HFillSpacer
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.InfoErrorMessageDefault
import com.mrv.wallet.ui.compose.components.InfoText
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.cell.CellUniversal
import com.mrv.wallet.ui.compose.components.title3_leah
import io.horizontalsystems.marketkit.models.BlockchainType
import kotlinx.parcelize.Parcelize

class SelectContactFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        SelectContactScreen(navController, navController.getInput())
    }

    @Parcelize
    data class Input(val selected: Contact?, val blockchainType: BlockchainType?) : Parcelable

    @Parcelize
    data class Result(val contact: Contact?) : Parcelable

}

@Composable
fun SelectContactScreen(navController: NavController, input: SelectContactFragment.Input?) {
    val viewModel = viewModel<SelectContactViewModel>(initializer = SelectContactViewModel.init(input?.selected, input?.blockchainType))
    val uiState = viewModel.uiState

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = {
                    title3_leah(text = stringResource(id = R.string.Contacts))
                },
                navigationIcon = {
                    HsBackButton(onClick = navController::popBackStack)
                },
            )
        }
    ) {
        if (uiState.items.isEmpty()) {
            Column(modifier = Modifier.padding(it)) {
                InfoText(text = stringResource(id = R.string.Transactions_Filter_ChooseContact_Hint))
                InfoErrorMessageDefault(
                    painter = painterResource(id = R.drawable.ic_user_24),
                    text = stringResource(R.string.Transactions_Filter_ChooseContact_NoSuitableContact)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    InfoText(text = stringResource(id = R.string.Transactions_Filter_ChooseContact_Hint))
                }
                items(uiState.items) { contact ->
                    CellContact(contact, uiState.selected) {
                        navController.setNavigationResultX(SelectContactFragment.Result(contact))
                        navController.popBackStack()
                    }
                }
                item {
                    VSpacer(height = 32.dp)
                }
            }
        }
    }
}

@Composable
private fun CellContact(
    contact: Contact?,
    selected: Contact?,
    onClick: () -> Unit,
) {
    CellUniversal(
        onClick = onClick
    ) {
        Icon(
            painter = if (contact == null) {
                painterResource(id = R.drawable.icon_paper_contract_24)
            } else {
                painterResource(id = R.drawable.ic_user_24)
            },
            contentDescription = "",
            tint = ComposeAppTheme.colors.grey
        )
        HSpacer(width = 16.dp)
        body_leah(text = contact?.name ?: stringResource(id = R.string.SelectContacts_All))
        if (contact == selected) {
            HFillSpacer(minWidth = 8.dp)
            Icon(
                painter = painterResource(id = R.drawable.icon_check_1_24),
                contentDescription = "selected",
                tint = ComposeAppTheme.colors.jacob
            )
        }
    }
}
