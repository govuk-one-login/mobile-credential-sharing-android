package uk.gov.onelogin.sharing.testapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.waterfallPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.toPersistentList
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination

@Composable
fun MainActivityContent(
    appGraph: CredentialSharingAppGraph,
    currentTab: PrimaryTabDestination,
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
    onUpdateTabDestination: (PrimaryTabDestination) -> Unit = {}
) {
    MainActivityContentUi(
        currentTab = currentTab,
        modifier = modifier,
        onSelectTab = { destination ->
            navController.navigate(destination)
            onUpdateTabDestination(destination)
        },
        navHost = {
            AppNavHost(
                navController = navController,
                startDestination = startDestination,
                appGraph = appGraph
            )
        }
    )
}

@Composable
fun MainActivityContentUi(
    currentTab: PrimaryTabDestination,
    modifier: Modifier = Modifier,
    onSelectTab: (PrimaryTabDestination) -> Unit = {},
    navHost: @Composable (Modifier) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TestWrapperTopBar(
                destinations = PrimaryTabDestination.entries().toPersistentList(),
                currentDestination = currentTab,
                modifier = Modifier.statusBarsPadding(),
                updateCurrentDestination = onSelectTab
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .waterfallPadding()
                .padding(contentPadding)
        ) {
            navHost(Modifier.fillMaxSize())
        }
    }
}

@Composable
@Preview
internal fun MainActivityContentUiPreview(
    @PreviewParameter(MainActivityContentPreviewParameters::class)
    currentTabDestination: PrimaryTabDestination
) {
    GdsTheme {
        MainActivityContentUi(
            currentTab = currentTabDestination,
            onSelectTab = {},
            navHost = {}
        )
    }
}
