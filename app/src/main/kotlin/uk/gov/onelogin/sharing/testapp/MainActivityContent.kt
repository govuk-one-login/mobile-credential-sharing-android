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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.toPersistentList
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination

@Composable
fun MainActivityContent(
    currentTab: PrimaryTabDestination,
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
    onUpdateTabDestination: (PrimaryTabDestination) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TestWrapperTopBar(
                destinations = PrimaryTabDestination.entries().toPersistentList(),
                currentDestination = currentTab,
                modifier = Modifier
                    .statusBarsPadding(),
                updateCurrentDestination = {
                    navController.navigate(it)
                    onUpdateTabDestination(it)
                }
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
            AppNavHost(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}

@Composable
@Preview
internal fun MainActivityContentPreview(
    @PreviewParameter(MainActivityContentPreviewParameters::class)
    currentTabDestination: PrimaryTabDestination
) {
    GdsTheme {
        MainActivityContent(
            navController = rememberNavController(),
            currentTab = currentTabDestination,
            startDestination = currentTabDestination
        )
    }
}
