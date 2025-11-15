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
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.toPersistentList

@Composable
fun MainActivityContent(
    currentTab: PrimaryTabDestination,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: PrimaryTabDestination = PrimaryTabDestination.Holder,
    onUpdateTabDestination: (PrimaryTabDestination) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TestWrapperTopBar(
                destinations = PrimaryTabDestination.entries().toPersistentList(),
                currentDestination = currentTab,
                onNavigate = navController::navigate,
                modifier = Modifier
                    .statusBarsPadding(),
                updateCurrentDestination = onUpdateTabDestination
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
