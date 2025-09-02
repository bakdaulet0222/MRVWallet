package com.mrv.wallet.modules.market.tvl

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrv.wallet.R
import com.mrv.wallet.core.providers.Translator
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.core.stats.statType
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.market.ImageSource
import com.mrv.wallet.modules.market.MarketModule
import com.mrv.wallet.modules.market.tvl.TvlModule.SelectorDialogState
import com.mrv.wallet.modules.market.tvl.TvlModule.TvlDiffType
import com.mrv.wallet.ui.compose.Select
import io.horizontalsystems.marketkit.models.HsTimePeriod
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class TvlViewModel(
    private val service: TvlService,
    private val tvlViewItemFactory: TvlViewItemFactory,
) : ViewModel() {

    private var tvlDiffType: TvlDiffType = TvlDiffType.Percent
        set(value) {
            field = value
            tvlDiffTypeLiveData.postValue(value)
        }
    private var tvlItems: List<TvlModule.MarketTvlItem> = listOf()

    val isRefreshingLiveData = MutableLiveData<Boolean>()
    val tvlLiveData = MutableLiveData<TvlModule.TvlData>()
    val tvlDiffTypeLiveData = MutableLiveData(tvlDiffType)
    val viewStateLiveData = MutableLiveData<ViewState>(ViewState.Loading)
    val chainSelectorDialogStateLiveData = MutableLiveData<SelectorDialogState>()

    var header = MarketModule.Header(
        title = Translator.getString(R.string.MarketGlobalMetrics_TvlInDefi),
        description = Translator.getString(R.string.MarketGlobalMetrics_TvlInDefiDescription),
        icon = ImageSource.Remote("https://cdn.blocksdecoded.com/header-images/tvl@3x.png")
    )

    init {
        viewModelScope.launch {
            service.marketTvlItemsObservable.asFlow().collect { tvlItemsDataState ->
                tvlItemsDataState.viewState?.let {
                    viewStateLiveData.postValue(it)
                }

                tvlItemsDataState.dataOrNull?.let {
                    tvlItems = it
                    syncTvlItems(it)
                }
            }
        }

        service.start()
    }

    private fun syncTvlItems(tvlItems: List<TvlModule.MarketTvlItem>) {
        tvlLiveData.postValue(
            tvlViewItemFactory.tvlData(service.chain, service.chains, service.sortDescending, tvlItems)
        )
    }

    private fun refreshWithMinLoadingSpinnerPeriod() {
        service.refresh()
        viewModelScope.launch {
            isRefreshingLiveData.postValue(true)
            delay(1000)
            isRefreshingLiveData.postValue(false)
        }
    }

    fun onSelectChain(chain: TvlModule.Chain) {
        service.chain = chain
        chainSelectorDialogStateLiveData.postValue(SelectorDialogState.Closed)

        stat(page = StatPage.GlobalMetricsTvlInDefi, event = StatEvent.SwitchTvlChain(chain.name))
    }

    fun onToggleSortType() {
        service.sortDescending = !service.sortDescending

        stat(page = StatPage.GlobalMetricsTvlInDefi, event = StatEvent.ToggleSortDirection)
    }

    fun onToggleTvlDiffType() {
        tvlDiffType = if (tvlDiffType == TvlDiffType.Percent) TvlDiffType.Currency else TvlDiffType.Percent

        stat(
            page = StatPage.GlobalMetricsTvlInDefi,
            event = StatEvent.ToggleTvlField(tvlDiffType.statType)
        )
    }

    fun onClickChainSelector() {
        chainSelectorDialogStateLiveData.postValue(
            SelectorDialogState.Opened(Select(service.chain, service.chains))
        )
    }

    fun onChainSelectorDialogDismiss() {
        chainSelectorDialogStateLiveData.postValue(SelectorDialogState.Closed)
    }

    fun refresh() {
        refreshWithMinLoadingSpinnerPeriod()
    }

    fun onErrorClick() {
        refreshWithMinLoadingSpinnerPeriod()
    }

    override fun onCleared() {
        service.stop()
    }

    fun onSelectChartInterval(chartInterval: HsTimePeriod?) {
        service.updateChartInterval(chartInterval)
    }
}
