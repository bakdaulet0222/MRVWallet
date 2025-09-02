package com.mrv.wallet.modules.settings.guides

import com.mrv.wallet.core.managers.ConnectivityManager
import com.mrv.wallet.core.managers.GuidesManager
import com.mrv.wallet.core.managers.LanguageManager
import com.mrv.wallet.core.retryWhen
import com.mrv.wallet.entities.DataState
import com.mrv.wallet.entities.GuideCategory
import com.mrv.wallet.entities.GuideCategoryMultiLang
import com.mrv.wallet.entities.GuideSection
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await

class GuidesRepository(
        private val guidesManager: GuidesManager,
        private val connectivityManager: ConnectivityManager,
        private val languageManager: LanguageManager
        ) {

    val guideCategories: Observable<DataState<List<GuideCategory>>>
        get() = guideCategoriesSubject

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val guideCategoriesSubject = BehaviorSubject.create<DataState<List<GuideCategory>>>()
    private val retryLimit = 3

    init {
        fetch()

        coroutineScope.launch {
            connectivityManager.networkAvailabilitySignal.asFlow().collect {
                if (connectivityManager.isConnected && guideCategoriesSubject.value is DataState.Error) {
                    fetch()
                }
            }
        }
    }

    fun clear() {
        coroutineScope.cancel()
    }

    private fun fetch() {
        guideCategoriesSubject.onNext(DataState.Loading)

        coroutineScope.launch {
            try {
                val guideCategories = retryWhen(
                    times = retryLimit,
                    predicate = { it is AssertionError }
                ) {
                    guidesManager.getGuideCategories().await()
                }

                val categories = getCategoriesByLocalLanguage(guideCategories, languageManager.currentLocale.language, languageManager.fallbackLocale.language)
                guideCategoriesSubject.onNext(DataState.Success(categories))
            } catch (e: Throwable) {
                guideCategoriesSubject.onNext(DataState.Error(e))
            }
        }
    }

    private fun getCategoriesByLocalLanguage(categoriesMultiLanguage: Array<GuideCategoryMultiLang>, language: String, fallbackLanguage: String) =
        categoriesMultiLanguage.map { categoriesMultiLang ->
            val categoryTitle = categoriesMultiLang.category[language] ?: categoriesMultiLang.category[fallbackLanguage] ?: ""

            val sections = categoriesMultiLang.sections.map { sectionMultiLang ->
                val sectionTitle = sectionMultiLang.title[language] ?: sectionMultiLang.title[fallbackLanguage] ?: ""
                val items = sectionMultiLang.items.mapNotNull {
                    it[language] ?: it[fallbackLanguage]
                }
                GuideSection(sectionTitle, items)
            }
            GuideCategory(categoryTitle, sections)
        }
}
