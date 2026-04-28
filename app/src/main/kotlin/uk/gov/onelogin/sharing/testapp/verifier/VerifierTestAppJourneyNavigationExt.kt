package uk.gov.onelogin.sharing.testapp.verifier

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.reflect.typeOf
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest.Companion.VerificationRequestType
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier

object VerifierTestAppJourneyNavigationExt {
    fun NavController.navigateToTestAppVerifierJourney(
        request: VerificationRequest,
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        VerifierTestAppJourney(request = request),
        options
    )

    internal fun NavGraphBuilder.configureVerifierJourneyWrapper(
        navController: NavController,
        requestToVerifier: (VerificationRequest) -> CredentialVerifier
    ) {
        composable<VerifierTestAppJourney>(
            typeMap = mapOf(
                typeOf<VerificationRequest>() to VerificationRequestType
            )
        ) { navBackStackEntry ->
            val arguments: VerifierTestAppJourney = navBackStackEntry.toRoute()
            val verifier = remember { requestToVerifier(arguments.request) }

            VerifierTestAppJourneyScreen(
                verifier = verifier,
                modifier = Modifier.fillMaxSize()
            ) {
                verifier.orchestrator.cancel()
                navController.popBackStack()
            }
        }
    }
}
