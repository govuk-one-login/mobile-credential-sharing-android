package uk.gov.onelogin.sharing.verifier.error

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.verifier.VerifierRoutes

object UnrecoverableVerifierErrorNavigationExt {
    fun NavController.navigateToUnrecoverableVerifierError(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(UnrecoverableVerifierErrorRoute, options)

    internal fun NavGraphBuilder.configureUnrecoverableVerifierError(navController: NavController) {
        composable<UnrecoverableVerifierErrorRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }
            UnrecoverableVerifierErrorScreen(
                onExitJourney = {
                    scope.launch {
                        navController.popBackStack(VerifierRoutes, inclusive = true)
                    }
                }
            )
        }
    }
}
