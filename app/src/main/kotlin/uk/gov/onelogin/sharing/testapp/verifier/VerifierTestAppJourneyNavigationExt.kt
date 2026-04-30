package uk.gov.onelogin.sharing.testapp.verifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.reflect.typeOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest.Companion.VerificationRequestType
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier

object VerifierTestAppJourneyNavigationExt {
    fun NavController.navigateToTestAppVerifierJourney(
        request: VerificationRequest,
        options: NavOptionsBuilder.() -> Unit = {},
    ) = navigate(
        VerifierTestAppJourney(request = request),
        options
    )

    internal fun NavGraphBuilder.configureVerifierJourneyWrapper(
        navController: NavController,
        requestToVerifier: (VerificationRequest) -> CredentialVerifier,
    ) {
        composable<VerifierTestAppJourney>(
            typeMap = mapOf(
                typeOf<VerificationRequest>() to VerificationRequestType
            )
        ) { navBackStackEntry ->
            val arguments: VerifierTestAppJourney = navBackStackEntry.toRoute()
            val verifier by produceCredentialVerifier(arguments.request, requestToVerifier)
            val scope = rememberCoroutineScope { Dispatchers.Main }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                verifier?.let { verifier ->
                    VerifierTestAppJourneyScreen(
                        verifier = verifier,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        scope.launch {
                            verifier.orchestrator.cancel()
                            navController.popBackStack()
                        }
                    }
                } ?: CircularProgressIndicator()
            }
        }
    }

    @Composable
    private fun produceCredentialVerifier(
        request: VerificationRequest,
        requestToVerifier: (VerificationRequest) -> CredentialVerifier,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) = produceState<CredentialVerifier?>(null, request, requestToVerifier) {
        value = withContext(dispatcher) { requestToVerifier(request) }
    }
}
