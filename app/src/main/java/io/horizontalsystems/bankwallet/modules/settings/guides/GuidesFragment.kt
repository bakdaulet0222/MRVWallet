package com.mrv.wallet.modules.settings.guides

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.LocalizedException
import com.mrv.wallet.core.slideFromRight
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.entities.ViewState
import com.mrv.wallet.modules.coin.overview.ui.Loading
import com.mrv.wallet.modules.markdown.MarkdownFragment
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.HFillSpacer
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.HsDivider
import com.mrv.wallet.ui.compose.components.ScreenMessageWithAction
import com.mrv.wallet.ui.compose.components.ScrollableTabs
import com.mrv.wallet.ui.compose.components.TabItem
import com.mrv.wallet.ui.compose.components.body_leah
import com.mrv.wallet.ui.compose.components.cell.CellUniversal
import com.mrv.wallet.ui.compose.components.headline2_leah
import java.net.UnknownHostException

class GuidesFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        GuidesScreen(navController)
    }

}

@Composable
fun GuidesScreen(navController: NavController) {
    val viewModel = viewModel<GuidesViewModel>(factory = GuidesModule.Factory())

    val uiState = viewModel.uiState

    val viewState = uiState.viewState
    val categories = uiState.categories
    val selectedCategory = uiState.selectedCategory
    val expandedSections = uiState.expandedSections

    Column(
        modifier = Modifier
            .background(color = ComposeAppTheme.colors.tyler)
            .navigationBarsPadding()
    ) {
        AppBar(
            title = stringResource(R.string.Guides_Title),
            navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack() })
            }
        )

        Crossfade(viewState) { viewState ->
            when (viewState) {
                ViewState.Loading -> {
                    Loading()
                }

                is ViewState.Error -> {
                    val s = when (val error = viewState.t) {
                        is UnknownHostException -> stringResource(R.string.Hud_Text_NoInternet)
                        is LocalizedException -> stringResource(error.errorTextRes)
                        else -> stringResource(R.string.Hud_UnknownError, error)
                    }

                    ScreenMessageWithAction(s, R.drawable.ic_error_48)
                }

                ViewState.Success -> {
                    if (selectedCategory != null) {
                        Column {
                            val tabItems = categories.map { TabItem(it.category, it == selectedCategory, it) }
                            ScrollableTabs(tabItems) { tab ->
                                viewModel.onSelectCategory(tab)
                            }
                            val listState = rememberSaveable(
                                selectedCategory,
                                saver = LazyListState.Saver
                            ) {
                                LazyListState()
                            }
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(bottom = 32.dp)
                            ) {
                                val sections = selectedCategory.sections
                                val sectionsSize = sections.size

                                sections.forEachIndexed { i, section ->
                                    val lastSection = i == sectionsSize - 1
                                    val sectionTitle = section.title
                                    val expanded = expandedSections.contains(sectionTitle)
                                    item {
                                        CellUniversal(
                                            borderTop = i != 0,
                                            color = ComposeAppTheme.colors.lawrence,
                                            onClick = {
                                                viewModel.toggleSection(sectionTitle, expanded)
                                            }
                                        ) {
                                            headline2_leah(sectionTitle)
                                            HFillSpacer(8.dp)
                                            val iconId = if (expanded) {
                                                R.drawable.ic_arrow_big_up_20
                                            } else {
                                                R.drawable.ic_arrow_big_down_20
                                            }
                                            Icon(
                                                painter = painterResource(iconId),
                                                contentDescription = null,
                                                tint = ComposeAppTheme.colors.grey
                                            )
                                        }
                                    }
                                    if (expanded) {
                                        itemsIndexed(section.items) { j, guide ->
                                            CellUniversal(
                                                borderTop = j != 0,
                                                onClick = {
                                                    navController.slideFromRight(
                                                        R.id.markdownFragment,
                                                        MarkdownFragment.Input(guide.markdown, true)
                                                    )

                                                    stat(page = StatPage.Academy, event = StatEvent.OpenArticle(guide.markdown))
                                                }
                                            ) {
                                                body_leah(guide.title)
                                            }
                                        }
                                        if (lastSection) {
                                            item {
                                                HsDivider()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
