package uk.gov.onelogin.sharing.testapp.verifier

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.reflect.typeOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        requestToVerifier: (Context, VerificationRequest) -> CredentialVerifier
    ) {
        composable<VerifierTestAppJourney>(
            typeMap = mapOf(
                typeOf<VerificationRequest>() to VerificationRequestType
            )
        ) { navBackStackEntry ->
            val context = LocalContext.current
            val arguments: VerifierTestAppJourney = navBackStackEntry.toRoute()
            val verifier by produceCredentialVerifier(context, arguments.request, requestToVerifier)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                verifier?.let { verifier ->
                    VerifierTestAppJourneyScreen(
                        verifier = verifier,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: CircularProgressIndicator()
            }
        }
    }

    @Composable
    private fun produceCredentialVerifier(
        context: Context,
        request: VerificationRequest,
        requestToVerifier: (Context, VerificationRequest) -> CredentialVerifier,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) = produceState<CredentialVerifier?>(null, request, requestToVerifier) {
        value = withContext(dispatcher) { requestToVerifier(context, request) }
    }
}
