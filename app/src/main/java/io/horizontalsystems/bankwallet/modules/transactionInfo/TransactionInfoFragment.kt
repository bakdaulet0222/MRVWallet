package com.mrv.wallet.modules.transactionInfo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEntity
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.modules.coin.CoinFragment
import com.mrv.wallet.modules.transactions.TransactionsModule
import com.mrv.wallet.modules.transactions.TransactionsViewModel
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.TranslatableString
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.mrv.wallet.ui.compose.components.DescriptionCell
import com.mrv.wallet.ui.compose.components.MenuItem
import com.mrv.wallet.ui.compose.components.PriceWithToggleCell
import com.mrv.wallet.ui.compose.components.SectionTitleCell
import com.mrv.wallet.ui.compose.components.TitleAndValueCell
import com.mrv.wallet.ui.compose.components.TransactionAmountCell
import com.mrv.wallet.ui.compose.components.TransactionInfoAddressCell
import com.mrv.wallet.ui.compose.components.TransactionInfoBtcLockCell
import com.mrv.wallet.ui.compose.components.TransactionInfoCancelCell
import com.mrv.wallet.ui.compose.components.TransactionInfoContactCell
import com.mrv.wallet.ui.compose.components.TransactionInfoDoubleSpendCell
import com.mrv.wallet.ui.compose.components.TransactionInfoExplorerCell
import com.mrv.wallet.ui.compose.components.TransactionInfoRawTransaction
import com.mrv.wallet.ui.compose.components.TransactionInfoSentToSelfCell
import com.mrv.wallet.ui.compose.components.TransactionInfoSpeedUpCell
import com.mrv.wallet.ui.compose.components.TransactionInfoStatusCell
import com.mrv.wallet.ui.compose.components.TransactionInfoTransactionHashCell
import com.mrv.wallet.ui.compose.components.TransactionNftAmountCell
import com.mrv.wallet.ui.compose.components.WarningMessageCell

class TransactionInfoFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val viewModelTxs: TransactionsViewModel? = try {
            navGraphViewModels<TransactionsViewModel>(R.id.mainFragment) { TransactionsModule.Factory() }.value
        } catch (e: IllegalStateException) {
            Toast.makeText(App.instance, "ViewModel is Null", Toast.LENGTH_SHORT).show()
            null
        }

        val transactionRecord = viewModelTxs?.tmpTransactionRecordToShow
        if (transactionRecord == null) {
            navController.popBackStack(R.id.transactionInfoFragment, true)
            return
        }

        val viewModel by navGraphViewModels<TransactionInfoViewModel>(R.id.transactionInfoFragment) {
            TransactionInfoModule.Factory(transactionRecord)
        }

        TransactionInfoScreen(viewModel, navController)
    }

}

@Composable
fun TransactionInfoScreen(
    viewModel: TransactionInfoViewModel,
    navController: NavController
) {

    Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = stringResource(R.string.TransactionInfo_Title),
            menuItems = listOf(
                MenuItem(
                    title = TranslatableString.ResString(R.string.Button_Close),
                    icon = R.drawable.ic_close,
                    onClick = {
                        navController.popBackStack()
                    }
                )
            )
        )
        TransactionInfo(viewModel, navController)
    }
}

@Composable
fun TransactionInfo(
    viewModel: TransactionInfoViewModel,
    navController: NavController
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)) {
        items(viewModel.viewItems) { section ->
            TransactionInfoSection(section, navController, viewModel::getRawTransaction)
        }
    }
}

@Composable
fun TransactionInfoSection(
    section: List<TransactionInfoViewItem>,
    navController: NavController,
    getRawTransaction: () -> String?
) {
    //items without background
    if (section.size == 1) {
        when (val item = section[0]) {
            is TransactionInfoViewItem.WarningMessage -> {
                WarningMessageCell(item.message)
                return
            }
            is TransactionInfoViewItem.Description -> {
                DescriptionCell(text = item.text)
                return
            }
            else -> {
                //do nothing
            }
        }
    }

    CellUniversalLawrenceSection(
        buildList {
            for (viewItem in section) {
                when (viewItem) {
                    is TransactionInfoViewItem.Transaction -> {
                        add {
                            SectionTitleCell(title = viewItem.leftValue, value = viewItem.rightValue, iconResId = viewItem.icon)
                        }
                    }

                    is TransactionInfoViewItem.Amount -> {
                        add {
                            TransactionAmountCell(
                                amountType = viewItem.amountType,
                                fiatAmount = viewItem.fiatValue,
                                coinAmount = viewItem.coinValue,
                                coinIconUrl = viewItem.coinIconUrl,
                                alternativeCoinIconUrl = viewItem.alternativeCoinIconUrl,
                                badge = viewItem.badge,
                                coinIconPlaceholder = viewItem.coinIconPlaceholder,
                                onClick = viewItem.coinUid?.let {
                                    {
                                        navController.slideFromRight(R.id.coinFragment, CoinFragment.Input(it))

                                        stat(
                                            page = StatPage.TransactionInfo,
                                            event = StatEvent.OpenCoin(it)
                                        )
                                    }
                                }
                            )
                        }
                    }

                    is TransactionInfoViewItem.NftAmount -> {
                        add {
                            TransactionNftAmountCell(
                                viewItem.title,
                                viewItem.nftValue,
                                viewItem.nftName,
                                viewItem.iconUrl,
                                viewItem.iconPlaceholder,
                                viewItem.badge,
                            )
                        }
                    }

                    is TransactionInfoViewItem.Value -> {
                        add {
                            TitleAndValueCell(
                                title = viewItem.title,
                                value = viewItem.value,
                            )
                        }
                    }

                    is TransactionInfoViewItem.PriceWithToggle -> {
                        add {
                            PriceWithToggleCell(
                                title = viewItem.title,
                                valueOne = viewItem.valueTwo,
                                valueTwo = viewItem.valueOne
                            )
                        }
                    }

                    is TransactionInfoViewItem.Address -> {
                        add {
                            TransactionInfoAddressCell(
                                title = viewItem.title,
                                value = viewItem.value,
                                showAdd = viewItem.showAdd,
                                blockchainType = viewItem.blockchainType,
                                navController = navController,
                                onCopy = {
                                    stat(
                                        page = StatPage.TransactionInfo,
                                        event = StatEvent.Copy(StatEntity.Address),
                                        section = viewItem.statSection
                                    )
                                },
                                onAddToExisting = {
                                    stat(
                                        page = StatPage.TransactionInfo,
                                        event = StatEvent.Open(StatPage.ContactAddToExisting),
                                        section = viewItem.statSection
                                    )
                                },
                                onAddToNew = {
                                    stat(
                                        page = StatPage.TransactionInfo,
                                        event = StatEvent.Open(StatPage.ContactNew),
                                        section = viewItem.statSection
                                    )
                                }
                            )
                        }
                    }

                    is TransactionInfoViewItem.ContactItem -> {
                        add {
                            TransactionInfoContactCell(viewItem.contact.name)
                        }
                    }

                    is TransactionInfoViewItem.Status -> {
                        add {
                            TransactionInfoStatusCell(status = viewItem.status, navController = navController)
                        }
                    }

                    is TransactionInfoViewItem.SpeedUpCancel -> {
                        add {
                            TransactionInfoSpeedUpCell(
                                transactionHash = viewItem.transactionHash,
                                blockchainType = viewItem.blockchainType,
                                navController = navController
                            )
                        }
                        add {
                            TransactionInfoCancelCell(
                                transactionHash = viewItem.transactionHash,
                                blockchainType = viewItem.blockchainType,
                                navController = navController
                            )
                        }
                    }

                    is TransactionInfoViewItem.TransactionHash -> {
                        add {
                            TransactionInfoTransactionHashCell(transactionHash = viewItem.transactionHash)
                        }
                    }

                    is TransactionInfoViewItem.Explorer -> {
                        viewItem.url?.let {
                            add {
                                TransactionInfoExplorerCell(title = viewItem.title, url = viewItem.url)
                            }
                        }
                    }

                    is TransactionInfoViewItem.RawTransaction -> {
                        add {
                            TransactionInfoRawTransaction(rawTransaction = getRawTransaction)
                        }
                    }

                    is TransactionInfoViewItem.LockState -> {
                        add {
                            TransactionInfoBtcLockCell(lockState = viewItem, navController = navController)
                        }
                    }

                    is TransactionInfoViewItem.DoubleSpend -> {
                        add {
                            TransactionInfoDoubleSpendCell(
                                transactionHash = viewItem.transactionHash,
                                conflictingHash = viewItem.conflictingHash,
                                navController = navController
                            )
                        }
                    }

                    is TransactionInfoViewItem.SentToSelf -> {
                        add {
                            TransactionInfoSentToSelfCell()
                        }
                    }

                    is TransactionInfoViewItem.Description -> {

                    }
                    is TransactionInfoViewItem.WarningMessage -> {

                    }
                }
            }
        }
    )
}

