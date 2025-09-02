package com.mrv.wallet.modules.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mrv.wallet.R
import com.mrv.wallet.core.BaseComposeFragment
import com.mrv.wallet.core.composablePage
import com.mrv.wallet.core.composablePopup
import com.mrv.wallet.core.stats.StatEvent
import com.mrv.wallet.core.stats.StatPage
import com.mrv.wallet.core.stats.stat
import com.mrv.wallet.modules.releasenotes.ReleaseNotesScreen
import com.mrv.wallet.modules.settings.appstatus.AppStatusScreen
import com.mrv.wallet.modules.settings.main.HsSettingCell
import com.mrv.wallet.modules.settings.terms.TermsScreen
import com.mrv.wallet.ui.compose.ComposeAppTheme
import com.mrv.wallet.ui.compose.components.AppBar
import com.mrv.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.mrv.wallet.ui.compose.components.HsBackButton
import com.mrv.wallet.ui.compose.components.VSpacer
import com.mrv.wallet.ui.helpers.LinkHelper

class AboutFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        AboutNavHost(navController)
    }

}

private const val AboutPage = "about"
private const val ReleaseNotesPage = "release_notes"
private const val AppStatusPage = "app_status"
private const val TermsPage = "terms"

@Composable
private fun AboutNavHost(fragmentNavController: NavController) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AboutPage,
    ) {
        composable(AboutPage) {
            AboutScreen(
                navController,
                { fragmentNavController.popBackStack() }
            )
        }
        composablePage(ReleaseNotesPage) {
            ReleaseNotesScreen(false, { navController.popBackStack() })
        }
        composablePage(AppStatusPage) { AppStatusScreen(navController) }
        composablePopup(TermsPage) { TermsScreen(navController) }
    }
}

@Composable
private fun AboutScreen(
    navController: NavController,
    onBackPress: () -> Unit,
    aboutViewModel: AboutViewModel = viewModel(factory = AboutModule.Factory()),
) {
    Surface(color = ComposeAppTheme.colors.tyler) {
        Column {
            AppBar(
                title = stringResource(R.string.SettingsAboutApp_Title),
                navigationIcon = {
                    HsBackButton(onClick = onBackPress)
                }
            )

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(12.dp))
                SettingSections(aboutViewModel, navController)
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun SettingSections(
    viewModel: AboutViewModel,
    navController: NavController
) {

    val context = LocalContext.current
    val termsShowAlert = viewModel.termsShowAlert

    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_AppVersion,
                icon = R.drawable.ic_info_20,
                value = viewModel.appVersion,
                onClick = {
                    navController.navigate(ReleaseNotesPage)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.WhatsNew))
                }
            )
        }
    )

    Spacer(Modifier.height(32.dp))

    CellUniversalLawrenceSection(
        listOf({
            HsSettingCell(
                R.string.Settings_AppStatus,
                R.drawable.ic_app_status,
                onClick = {
                    navController.navigate(AppStatusPage)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.AppStatus))
                }
            )
        }, {
            HsSettingCell(
                R.string.Settings_Terms,
                R.drawable.ic_terms_20,
                showAlert = termsShowAlert,
                onClick = {
                    navController.navigate(TermsPage)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.Terms))
                }
            )
        })
    )

    Spacer(Modifier.height(32.dp))

    CellUniversalLawrenceSection(
        listOf({
            HsSettingCell(
                R.string.SettingsAboutApp_Github,
                R.drawable.ic_github_20,
                onClick = {

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalGithub))
                }
            )
        }, {
            HsSettingCell(
                R.string.SettingsAboutApp_Site,
                R.drawable.ic_globe,
                onClick = {
                    LinkHelper.openLinkInAppBrowser(context, viewModel.appWebPageLink)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalWebsite))
                }
            )
        })
    )

    VSpacer(32.dp)
}
