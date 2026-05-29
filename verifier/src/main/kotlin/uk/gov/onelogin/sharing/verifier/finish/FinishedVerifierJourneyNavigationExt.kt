package uk.gov.onelogin.sharing.verifier.finish

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse

object FinishedVerifierJourneyNavigationExt {
    fun NavController.navigateToFinishedVerifierJourney(
        response: DeviceResponse,
        options: NavOptionsBuilder.() -> Unit = {},
    ) = navigate(
        FinishedVerifierJourneyRoute(response = response),
        options
    )

    internal fun NavGraphBuilder.configureFinishedVerifierJourney(
        controller: NavController
    ) {
        composable<FinishedVerifierJourneyRoute> { backStackEntry ->
            val scope = rememberCoroutineScope { Dispatchers.Main }
            val args: FinishedVerifierJourneyRoute = backStackEntry.toRoute()

            FinishedVerifierJourneyScreen(
                response = args.response,
                onExitJourney = {
                    scope.launch {
                        controller.popBackStack()
                    }
                }
            )
        }
    }
}

