package com.mrv.wallet.modules.restoreaccount

import androidx.lifecycle.ViewModel
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.entities.AccountType
import com.mrv.wallet.modules.enablecoin.restoresettings.BirthdayHeightConfig

class RestoreViewModel: ViewModel() {

    var accountType: AccountType? = null
        private set

    var accountName: String = ""
        private set

    var manualBackup: Boolean = false
        private set

    var fileBackup: Boolean = false
        private set

    var birthdayHeightConfig: BirthdayHeightConfig? = null
        private set

    var statPage: StatPage? = null
        private set

    var cancelZCashConfig: Boolean = false

    fun setAccountData(accountType: AccountType, accountName: String, manualBackup: Boolean, fileBackup: Boolean, statPage: StatPage) {
        this.accountType = accountType
        this.accountName = accountName
        this.manualBackup = manualBackup
        this.fileBackup = fileBackup
        this.statPage = statPage
    }

    fun setZCashConfig(config: BirthdayHeightConfig?) {
        birthdayHeightConfig = config
    }

}