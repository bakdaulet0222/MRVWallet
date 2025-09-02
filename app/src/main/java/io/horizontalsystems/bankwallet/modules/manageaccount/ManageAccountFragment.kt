package com.mrv.wallet.modules.manageaccount

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.authorizedAction
import com.mrv.wallet.core.managers.FaqManager
import com.mrv.wallet.core.slideFromBottom
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEntity
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.entities.Account
import com.mrv.wallet.modules.balance.HeaderNote
import com.mrv.wallet.modules.balance.ui.NoteError
import com.mrv.wallet.modules.balance.ui.NoteWarning
import com.mrv.wallet.modules.manageaccount.ManageAccountModule.BackupItem
import com.mrv.wallet.modules.manageaccount.ManageAccountModule.KeyAction
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.ButtonSecondaryDefault
import com.mrv.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.mrv.wallet.ui.compose.components.FormsInput
import com.mrv.wallet.ui.compose.components.HSpacer
import com.mrv.wallet.ui.compose.components.HeaderText
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.HsImage
import com.mrv.wallet.ui.compose.components.InfoText
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.RowUniversal
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.compose.components.body_jacob
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.body_lucian
import io.horizontalsystems.core.helpers.HudHelper
import kotlinx.parcelize.Parcelize

class ManageAccountFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        withInput<Input>(navController) { input ->
            ManageAccountScreen(navController, input.accountId)
        }
    }

    @Parcelize
    data class Input(val accountId: String) : Parcelable
}

@Composable
fun ManageAccountScreen(navController: NavController, accountId: String) {
    val viewModel =
        viewModel<ManageAccountViewModel>(factory = ManageAccountModule.Factory(accountId))

    if (viewModel.viewState.closeScreen) {
        navController.popBackStack()
        viewModel.onClose()
    }

    Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = viewModel.viewState.title,
            navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack() })
            },
            menuItems = listOf(
                MenuItem(
                    title = TranslatableString.ResString(R.string.ManageAccount_Save),
                    onClick = { viewModel.onSave() },
                    enabled = viewModel.viewState.canSave,
                    tint = ComposeAppTheme.colors.jacob
                )
            )
        )

        Column {
            HeaderText(stringResource(id = R.string.ManageAccount_Name))

            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                initial = viewModel.viewState.title,
                hint = "",
                onValueChange = {
                    viewModel.onChange(it)

                    stat(
                        page = StatPage.ManageWallet,
                        event = StatEvent.Edit(StatEntity.WalletName)
                    )
                }
            )

            when (viewModel.viewState.headerNote) {
                HeaderNote.NonStandardAccount -> {
                    NoteError(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp),
                        text = stringResource(R.string.AccountRecovery_MigrationRequired),
                        onClick = {
                            FaqManager.showFaqPage(
                                navController,
                                FaqManager.faqPathMigrationRequired
                            )
                        }
                    )
                }

                HeaderNote.NonRecommendedAccount -> {
                    NoteWarning(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp),
                        text = stringResource(R.string.AccountRecovery_MigrationRecommended),
                        onClick = {
                            FaqManager.showFaqPage(
                                navController,
                                FaqManager.faqPathMigrationRecommended
                            )
                        },
                        onClose = null
                    )
                }

                HeaderNote.None -> Unit
            }

            KeyActions(viewModel, navController)

            if (viewModel.viewState.backupActions.isNotEmpty()) {
                BackupActions(
                    viewModel.viewState.backupActions,
                    viewModel.account,
                    navController
                )
            }

            VSpacer(32.dp)
            CellUniversalLawrenceSection(
                listOf {
                    RedActionItem(
                        title = stringResource(id = R.string.ManageAccount_Unlink),
                        icon = painterResource(id = R.drawable.ic_delete_20)
                    ) {
                        navController.slideFromBottom(
                            R.id.unlinkConfirmationDialog,
                            viewModel.account
                        )

                        stat(
                            page = StatPage.ManageWallet,
                            event = StatEvent.Open(StatPage.UnlinkWallet)
                        )
                    }
                })
            VSpacer(32.dp)
        }
    }
}

@Composable
private fun BackupActions(
    backupActions: List<BackupItem>,
    account: Account,
    navController: NavController
) {
    val actionItems = mutableListOf<@Composable () -> Unit>()
    val infoItems = mutableListOf<@Composable () -> Unit>()

    backupActions.forEach { action ->
        when (action) {
            is BackupItem.ManualBackup -> {
                actionItems.add {
                    YellowActionItem(
                        title = stringResource(id = R.string.ManageAccount_RecoveryPhraseBackup),
                        icon = painterResource(id = R.drawable.ic_edit_24),
                        attention = action.showAttention,
                        completed = action.completed
                    ) {
                        navController.authorizedAction {
                            navController.slideFromBottom(
                                R.id.backupKeyFragment,
                                account
                            )

                            stat(
                                page = StatPage.ManageWallet,
                                event = StatEvent.Open(StatPage.ManualBackup)
                            )
                        }
                    }
                }
            }

            is BackupItem.LocalBackup -> {
                actionItems.add {
                    YellowActionItem(
                        title = stringResource(id = R.string.ManageAccount_LocalBackup),
                        icon = painterResource(id = R.drawable.ic_file_24),
                        attention = action.showAttention
                    ) {
                        navController.authorizedAction {
                            navController.slideFromBottom(R.id.backupLocalFragment, account)

                            stat(
                                page = StatPage.ManageWallet,
                                event = StatEvent.Open(StatPage.FileBackup)
                            )
                        }
                    }
                }
            }

            is BackupItem.InfoText -> {
                infoItems.add {
                    InfoText(text = stringResource(action.textRes))
                }
            }
        }
    }
    if (actionItems.isNotEmpty()) {
        VSpacer(32.dp)
        CellUniversalLawrenceSection(actionItems)
    }
    infoItems.forEach {
        it.invoke()
    }

}

@Composable
private fun KeyActions(
    viewModel: ManageAccountViewModel,
    navController: NavController
) {
    val actionItems = mutableListOf<@Composable () -> Unit>()

    viewModel.viewState.keyActions.forEach { keyAction ->
        when (keyAction) {
            KeyAction.RecoveryPhrase -> {
                actionItems.add {
                    AccountActionItem(
                        title = stringResource(id = R.string.RecoveryPhrase_Title),
                        icon = painterResource(id = R.drawable.icon_paper_contract_20)
                    ) {
                        navController.authorizedAction {
                            navController.slideFromRight(
                                R.id.recoveryPhraseFragment,
                                viewModel.account
                            )

                            stat(
                                page = StatPage.ManageWallet,
                                event = StatEvent.Open(StatPage.RecoveryPhrase)
                            )
                        }
                    }
                }
            }

            KeyAction.PrivateKeys -> {
                actionItems.add {
                    AccountActionItem(
                        title = stringResource(id = R.string.PrivateKeys_Title),
                        icon = painterResource(id = R.drawable.ic_key_20)
                    ) {
                        navController.slideFromRight(
                            R.id.privateKeysFragment,
                            viewModel.account
                        )

                        stat(
                            page = StatPage.ManageWallet,
                            event = StatEvent.Open(StatPage.PrivateKeys)
                        )
                    }
                }
            }

            KeyAction.PublicKeys -> {
                actionItems.add {
                    AccountActionItem(
                        title = stringResource(id = R.string.PublicKeys_Title),
                        icon = painterResource(id = R.drawable.icon_binocule_20)
                    ) {
                        navController.slideFromRight(
                            R.id.publicKeysFragment,
                            viewModel.account
                        )

                        stat(
                            page = StatPage.ManageWallet,
                            event = StatEvent.Open(StatPage.PublicKeys)
                        )
                    }
                }
            }
        }
    }

    if (actionItems.isNotEmpty()) {
        VSpacer(32.dp)
        CellUniversalLawrenceSection(actionItems)
    }
}

@Composable
private fun AccountActionItem(
    title: String,
    icon: Painter? = null,
    coinIconUrl: String? = null,
    coinIconPlaceholder: Int? = null,
    attention: Boolean = false,
    badge: String? = null,
    onClick: (() -> Unit)? = null
) {

    RowUniversal(
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp),
                painter = icon,
                contentDescription = null,
                tint = ComposeAppTheme.colors.grey
            )
        }

        if (coinIconUrl != null) {
            HsImage(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(20.dp),
                url = coinIconUrl,
                placeholder = coinIconPlaceholder
            )
        }

        body_leah(
            modifier = Modifier.weight(1f),
            text = title,
        )

        if (attention) {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                painter = painterResource(id = R.drawable.ic_attention_20),
                contentDescription = null,
                tint = ComposeAppTheme.colors.lucian
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        badge?.let {
            val view = LocalView.current
            val clipboardManager = LocalClipboardManager.current

            ButtonSecondaryDefault(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = it,
                onClick = {
                    clipboardManager.setText(AnnotatedString(it))
                    HudHelper.showSuccessMessage(view, R.string.Hud_Text_Copied)
                }
            )
        }

        onClick?.let {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = ComposeAppTheme.colors.grey
            )
            HSpacer(16.dp)
        }
    }
}

@Composable
private fun YellowActionItem(
    title: String,
    icon: Painter? = null,
    attention: Boolean = false,
    completed: Boolean = false,
    onClick: (() -> Unit)? = null
) {

    RowUniversal(
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp),
                painter = icon,
                contentDescription = null,
                tint = ComposeAppTheme.colors.jacob
            )
        }

        body_jacob(
            modifier = Modifier.weight(1f),
            text = title,
        )

        if (attention) {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                painter = painterResource(id = R.drawable.ic_attention_20),
                contentDescription = null,
                tint = ComposeAppTheme.colors.lucian
            )
            HSpacer(6.dp)
        } else if (completed) {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                painter = painterResource(id = R.drawable.ic_checkmark_20),
                contentDescription = null,
                tint = ComposeAppTheme.colors.remus
            )
            HSpacer(6.dp)
        }
    }
}

@Composable
private fun RedActionItem(
    title: String,
    icon: Painter,
    onClick: () -> Unit
) {

    RowUniversal(
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(24.dp),
            painter = icon,
            contentDescription = null,
            tint = ComposeAppTheme.colors.lucian
        )

        body_lucian(
            text = title,
        )
    }
}
