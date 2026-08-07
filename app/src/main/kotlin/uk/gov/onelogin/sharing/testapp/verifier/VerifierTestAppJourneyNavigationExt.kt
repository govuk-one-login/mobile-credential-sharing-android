package uk.gov.onelogin.sharing.testapp.verifier

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.reflect.typeOf
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest.Companion.VerificationRequestType
import uk.gov.onelogin.sharing.sdk.api.verifier.VerificationSession

object VerifierTestAppJourneyNavigationExt {
    fun NavController.navigateToTestAppVerifierJourney(
        request: VerificationRequest,
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        VerifierTestAppJourney(request = request),
        options
    )

    internal fun NavGraphBuilder.configureVerifierJourneyWrapper(
        requestToSession: (Context, VerificationRequest) -> VerificationSession
    ) {
        composable<VerifierTestAppJourney>(
            typeMap = mapOf(
                typeOf<VerificationRequest>() to VerificationRequestType
            )
        ) { navBackStackEntry ->
            val context = LocalContext.current
            val arguments: VerifierTestAppJourney = navBackStackEntry.toRoute()

            val viewModel: VerifierJourneyViewModel = viewModel()
            viewModel.getSession(context, arguments.request, requestToSession)

            val session by viewModel.session.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                session?.let { verificationSession ->
                    VerifierTestAppJourneyScreen(
                        session = verificationSession,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: CircularProgressIndicator()
            }
        }
    }
}
