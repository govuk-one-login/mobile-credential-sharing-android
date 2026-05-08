package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.ui.impl.di.VerifierUiGraph
import uk.gov.onelogin.sharing.verifier.MonitorVerifierSessionState
import uk.gov.onelogin.sharing.verifier.VerifierRoutes
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes

/**
 * Composable entry point for the Verifier role (credential verification).
 *
 * Renders the complete Verifier UI flow including camera scanning and navigation,
 * allowing the app to request and verify credentials from holders.
 *
 * @param component The [CredentialVerifier] containing the app graph and verification request.
 * @param modifier Optional [Modifier] to apply to the root composable.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VerifyCredential(component: CredentialVerifier, modifier: Modifier = Modifier) {
    val uiGraph = remember(component.appGraph, component.orchestrator) {
        createGraphFactory<VerifierUiGraph.Factory>()
            .create(component.appGraph, component.orchestrator)
    }
    val navController = rememberNavController()
    val orchestrator = uiGraph.verifierOrchestrator()

    VerifyCredential(
        controller = navController,
        modifier = modifier,
        orchestrator = orchestrator,
        verifierSessionState = orchestrator.verifierSessionState,
        viewModelFactory = uiGraph.metroViewModelFactory
    )
}

@Composable
internal fun VerifyCredential(
    orchestrator: Orchestrator.Verifier,
    verifierSessionState: StateFlow<VerifierSessionState>,
    viewModelFactory: MetroViewModelFactory,
    modifier: Modifier = Modifier,
    controller: NavHostController = rememberNavController(),
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    MonitorVerifierSessionState(
        sessionState = verifierSessionState,
        controller = controller
    )

    val scope = rememberCoroutineScope { defaultDispatcher }
    LaunchedEffect(Unit) {
        scope.launch {
            orchestrator.start()
        }
    }

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides viewModelFactory
    ) {
        NavHost(
            navController = controller,
            startDestination = VerifierRoutes,
            modifier = modifier
        ) {
            configureVerifierRoutes(controller)
        }
    }
}
