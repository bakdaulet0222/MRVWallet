package com.mrv.wallet.modules.restoreaccount.restoreblockchains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.entities.AccountType
import com.mrv.wallet.modules.enablecoin.blockchaintokens.BlockchainTokensService
import com.mrv.wallet.modules.enablecoin.blockchaintokens.BlockchainTokensViewModel
import com.mrv.wallet.modules.enablecoin.restoresettings.RestoreSettingsService
import com.mrv.wallet.modules.enablecoin.restoresettings.RestoreSettingsViewModel
import com.mrv.wallet.modules.market.ImageSource

object RestoreBlockchainsModule {

    class Factory(
        private val accountName: String,
        private val accountType: AccountType,
        private val manualBackup: Boolean,
        private val fileBackup: Boolean,
        private val statPage: StatPage
    ) : ViewModelProvider.Factory {

        private val restoreSettingsService by lazy {
            RestoreSettingsService(App.restoreSettingsManager, App.zcashBirthdayProvider)
        }
        private val blockchainTokensService by lazy {
            BlockchainTokensService()
        }

        private val restoreSelectCoinsService by lazy {
            RestoreBlockchainsService(
                accountName,
                accountType,
                manualBackup,
                fileBackup,
                App.accountFactory,
                App.accountManager,
                App.walletManager,
                App.marketKit,
                App.tokenAutoEnableManager,
                blockchainTokensService,
                restoreSettingsService,
                statPage
            )
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                RestoreSettingsViewModel::class.java -> {
                    RestoreSettingsViewModel(
                        restoreSettingsService,
                        listOf(restoreSettingsService)
                    ) as T
                }
                RestoreBlockchainsViewModel::class.java -> {
                    RestoreBlockchainsViewModel(
                        restoreSelectCoinsService,
                        listOf(restoreSelectCoinsService)
                    ) as T
                }
                BlockchainTokensViewModel::class.java -> {
                    BlockchainTokensViewModel(blockchainTokensService) as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }
}

data class CoinViewItem<T>(
    val item: T,
    val imageSource: ImageSource,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
    val hasSettings: Boolean = false,
    val hasInfo: Boolean = false,
    val label: String? = null,
)
