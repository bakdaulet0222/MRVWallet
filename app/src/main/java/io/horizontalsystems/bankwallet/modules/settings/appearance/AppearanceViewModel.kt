package com.mrv.wallet.modules.settings.appearance

import androidx.lifecycle.viewModelScope
import com.mrv.wallet.core.ILocalStorage
import com.mrv.wallet.core.ViewModelUiState
import com.mrv.wallet.core.managers.CurrencyManager
import com.mrv.wallet.core.managers.LanguageManager
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statValue
import com.mrv.wallet.entities.LaunchPage
import com.mrv.wallet.modules.balance.BalanceViewType
import com.mrv.wallet.modules.balance.BalanceViewTypeManager
import com.mrv.wallet.modules.theme.ThemeService
import com.mrv.wallet.modules.theme.ThemeType
import com.mrv.wallet.ui.compose.Select
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class AppearanceViewModel(
    private val launchScreenService: LaunchScreenService,
    private val appIconService: AppIconService,
    private val themeService: ThemeService,
    private val balanceViewTypeManager: BalanceViewTypeManager,
    private val localStorage: ILocalStorage,
    private val languageManager: LanguageManager,
    private val currencyManager: CurrencyManager,
) : ViewModelUiState<AppearanceUIState>() {
    private var launchScreenOptions = launchScreenService.optionsFlow.value
    private var appIconOptions = appIconService.optionsFlow.value
    private var themeOptions = themeService.optionsFlow.value
    private var marketsTabHidden = !localStorage.marketsTabEnabled
    private var balanceTabButtonsHidden = !localStorage.balanceTabButtonsEnabled
    private var amountRoundingEnabled = localStorage.amountRoundingEnabled
    private var balanceViewTypeOptions = buildBalanceViewTypeSelect(balanceViewTypeManager.balanceViewTypeFlow.value)
    private var priceChangeInterval = localStorage.priceChangeInterval
    private var priceChangeIntervalOptions = buildPriceChangeIntervalSelect(priceChangeInterval)
    private val currentLanguageDisplayName: String
        get() = languageManager.currentLanguageName

    private val baseCurrencyCode: String
        get() = currencyManager.baseCurrency.code

    init {
        viewModelScope.launch {
            launchScreenService.optionsFlow
                .collect {
                    handleUpdatedLaunchScreenOptions(it)
                }
        }
        viewModelScope.launch {
            appIconService.optionsFlow
                .collect {
                    handleUpdatedAppIconOptions(it)
                }
        }
        viewModelScope.launch {
            themeService.optionsFlow
                .collect {
                    handleUpdatedThemeOptions(it)
                }
        }
        viewModelScope.launch {
            balanceViewTypeManager.balanceViewTypeFlow
                .collect {
                    handleUpdatedBalanceViewType(buildBalanceViewTypeSelect(it))
                }
        }
        viewModelScope.launch {
            currencyManager.baseCurrencyUpdatedSignal.asFlow().collect {
                emitState()
            }
        }
    }

    override fun createState() = AppearanceUIState(
        currentLanguage = currentLanguageDisplayName,
        baseCurrencyCode = baseCurrencyCode,
        launchScreenOptions = launchScreenOptions,
        appIconOptions = appIconOptions,
        themeOptions = themeOptions,
        balanceViewTypeOptions = balanceViewTypeOptions,
        marketsTabHidden = marketsTabHidden,
        balanceTabButtonsHidden = balanceTabButtonsHidden,
        selectedTheme = themeService.selectedTheme,
        selectedLaunchScreen = launchScreenService.selectedLaunchScreen,
        selectedBalanceViewType = balanceViewTypeManager.balanceViewType,
        priceChangeInterval = priceChangeInterval,
        priceChangeIntervalOptions = priceChangeIntervalOptions,
        amountRoundingEnabled = amountRoundingEnabled
    )

    private fun buildBalanceViewTypeSelect(value: BalanceViewType): Select<BalanceViewType> {
        return Select(value, balanceViewTypeManager.viewTypes)
    }

    private fun buildPriceChangeIntervalSelect(value: PriceChangeInterval): Select<PriceChangeInterval> {
        return Select(value, PriceChangeInterval.entries)
    }

    private fun handleUpdatedLaunchScreenOptions(launchScreenOptions: Select<LaunchPage>) {
        this.launchScreenOptions = launchScreenOptions
        emitState()
    }

    private fun handleUpdatedAppIconOptions(appIconOptions: Select<AppIcon>) {
        this.appIconOptions = appIconOptions
        emitState()
    }

    private fun handleUpdatedThemeOptions(themeOptions: Select<ThemeType>) {
        this.themeOptions = themeOptions
        emitState()
    }

    private fun handleUpdatedBalanceViewType(balanceViewTypeOptions: Select<BalanceViewType>) {
        this.balanceViewTypeOptions = balanceViewTypeOptions
        emitState()
    }

    fun onEnterLaunchPage(launchPage: LaunchPage) {
        launchScreenService.setLaunchScreen(launchPage)

        stat(page = StatPage.Appearance, event = StatEvent.SelectLaunchScreen(launchPage.statValue))
    }

    fun onEnterAppIcon(enabledAppIcon: AppIcon) {
        appIconService.setAppIcon(enabledAppIcon)

        stat(
            page = StatPage.Appearance,
            event = StatEvent.SelectAppIcon(enabledAppIcon.titleText.lowercase())
        )
    }

    fun onEnterTheme(themeType: ThemeType) {
        themeService.setThemeType(themeType)

        stat(page = StatPage.Appearance, event = StatEvent.SelectTheme(themeType.statValue))
    }

    fun onEnterBalanceViewType(viewType: BalanceViewType) {
        balanceViewTypeManager.setViewType(viewType)

        stat(page = StatPage.Appearance, event = StatEvent.SelectBalanceValue(viewType.statValue))
    }

    fun onSetMarketTabsHidden(hidden: Boolean) {
        if (hidden && (launchScreenOptions.selected == LaunchPage.Market || launchScreenOptions.selected == LaunchPage.Watchlist)) {
            launchScreenService.setLaunchScreen(LaunchPage.Auto)
        }
        localStorage.marketsTabEnabled = !hidden

        marketsTabHidden = hidden
        emitState()

        stat(page = StatPage.Appearance, event = StatEvent.ShowMarketsTab(shown = !hidden))
    }

    fun onSetBalanceTabButtonsHidden(hidden: Boolean) {
        localStorage.balanceTabButtonsEnabled = !hidden

        balanceTabButtonsHidden = hidden
        emitState()

        stat(page = StatPage.Appearance, event = StatEvent.HideBalanceButtons(shown = !hidden))
    }

    fun onAmountRoundingToggle(enabled: Boolean) {
        localStorage.amountRoundingEnabled = enabled
        amountRoundingEnabled = enabled
        emitState()
    }

    fun onSetPriceChangeInterval(priceChangeInterval: PriceChangeInterval) {
        localStorage.priceChangeInterval = priceChangeInterval

        this.priceChangeInterval = priceChangeInterval
        this.priceChangeIntervalOptions = buildPriceChangeIntervalSelect(priceChangeInterval)
        emitState()

        stat(
            page = StatPage.Appearance,
            event = StatEvent.SwitchPriceChangeMode(priceChangeInterval.statValue)
        )
    }

}

data class AppearanceUIState(
    val currentLanguage: String,
    val baseCurrencyCode: String,
    val launchScreenOptions: Select<LaunchPage>,
    val appIconOptions: Select<AppIcon>,
    val themeOptions: Select<ThemeType>,
    val balanceViewTypeOptions: Select<BalanceViewType>,
    val marketsTabHidden: Boolean,
    val balanceTabButtonsHidden: Boolean,
    val selectedTheme: ThemeType,
    val selectedLaunchScreen: LaunchPage,
    val selectedBalanceViewType: BalanceViewType,
    val priceChangeInterval: PriceChangeInterval,
    val priceChangeIntervalOptions: Select<PriceChangeInterval>,
    val amountRoundingEnabled: Boolean
)
