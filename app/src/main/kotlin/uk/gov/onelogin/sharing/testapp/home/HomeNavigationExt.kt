package uk.gov.onelogin.sharing.testapp.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

object HomeNavigationExt {

    internal fun NavGraphBuilder.configureTestAppHomeScreen(
        onStartHolderJourney: () -> Unit = {},
        onStartVerifierJourney: () -> Unit = {}
    ) {
        composable<HomeRoute> {
            TestAppScreen(
                modifier = Modifier.fillMaxSize(),
                onStartHolderJourney = onStartHolderJourney,
                onStartVerifierJourney = onStartVerifierJourney
            )
        }
    }
}
