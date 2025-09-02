package com.mrv.wallet.modules.enablecoin.restoresettings

import com.mrv.wallet.core.Clearable
import com.mrv.wallet.core.managers.RestoreSettingType
import com.mrv.wallet.core.managers.RestoreSettings
import com.mrv.wallet.core.managers.RestoreSettingsManager
import com.mrv.wallet.core.managers.ZcashBirthdayProvider
import com.mrv.wallet.core.restoreSettingTypes
import com.mrv.wallet.entities.Account
import com.mrv.wallet.entities.AccountOrigin
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.reactivex.subjects.PublishSubject

class RestoreSettingsService(
    private val manager: RestoreSettingsManager,
    private val zcashBirthdayProvider: ZcashBirthdayProvider
) : Clearable {

    val approveSettingsObservable = PublishSubject.create<TokenWithSettings>()
    val rejectApproveSettingsObservable = PublishSubject.create<Token>()
    val requestObservable = PublishSubject.create<Request>()

    fun approveSettings(token: Token, account: Account? = null) {
        val blockchainType = token.blockchainType

        if (account != null && account.origin == AccountOrigin.Created) {
            val settings = RestoreSettings()
            blockchainType.restoreSettingTypes.forEach { settingType ->
                manager.getSettingValueForCreatedAccount(settingType, blockchainType)?.let {
                    settings[settingType] = it
                }
            }
            approveSettingsObservable.onNext(TokenWithSettings(token, settings))
            return
        }

        val existingSettings = account?.let { manager.settings(it, blockchainType) } ?: RestoreSettings()

        if (blockchainType.restoreSettingTypes.contains(RestoreSettingType.BirthdayHeight)
            && existingSettings.birthdayHeight == null
        ) {
            requestObservable.onNext(Request(token, RequestType.BirthdayHeight))
            return
        }

        approveSettingsObservable.onNext(TokenWithSettings(token, RestoreSettings()))
    }

    fun save(settings: RestoreSettings, account: Account, blockchainType: BlockchainType) {
        manager.save(settings, account, blockchainType)
    }

    fun enter(config: BirthdayHeightConfig, token: Token) {
        val settings = RestoreSettings()
        settings.birthdayHeight = if (config.restoreAsNew) {
            when (token.blockchainType) {
                BlockchainType.Zcash -> zcashBirthdayProvider.getLatestCheckpointBlockHeight()
                BlockchainType.Monero -> -1
                else -> null
            }
        } else {
            config.birthdayHeight?.toLongOrNull()
        }

        val tokenWithSettings = TokenWithSettings(token, settings)
        approveSettingsObservable.onNext(tokenWithSettings)
    }

    fun cancel(token: Token) {
        rejectApproveSettingsObservable.onNext(token)
    }

    override fun clear() = Unit

    data class TokenWithSettings(val token: Token, val settings: RestoreSettings)
    data class Request(val token: Token, val requestType: RequestType)
    enum class RequestType {
        BirthdayHeight
    }
}
