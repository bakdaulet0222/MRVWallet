package com.mrv.wallet.core.managers

import androidx.navigation.NavController
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.mrv.wallet.R
import com.mrv.wallet.core.App
import com.mrv.wallet.core.slideFromBottom
import com.mrv.wallet.entities.Faq
import com.mrv.wallet.entities.FaqMap
import com.mrv.wallet.modules.markdown.MarkdownFragment
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.URL

object FaqManager {

    private const val FAQ_ASSETS_PATH = "common/faq_mrv.json"

    const val faqPathMigrationRequired = "management/migration_required.md"
    const val faqPathMigrationRecommended = "management/migration_recommended.md"
    const val faqPathPrivateKeys = "management/what-are-private-keys-mnemonic-phrase-wallet-seed.md"
    const val faqPathDefiRisks = "defi/defi-risks.md"

    fun showFaqPage(navController: NavController, path: String, language: String = "en") {
        // Формируем путь к файлу в assets на основе переданного пути
        val assetPath = "faq/$language/$path"
        // Передаем обычный путь, предполагая что MarkdownFragment сам умеет работать с assets
        navController.slideFromBottom(
            R.id.markdownFragment,
            MarkdownFragment.Input(assetPath, true, true)
        )
    }

    fun getFaqList(): Single<List<FaqMap>> {
        return Single.fromCallable {
            val inputStream = App.instance.assets.open(FAQ_ASSETS_PATH)
            val reader = InputStreamReader(inputStream)

            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

            val listType = object : TypeToken<List<FaqMap>>() {}.type
            val list: List<FaqMap> = gson.fromJson(reader, listType)

            reader.close()
            inputStream.close()

            list
        }
    }
}