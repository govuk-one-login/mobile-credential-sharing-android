package uk.gov.onelogin.sharing.ui.impl

import android.R.drawable.ic_menu_close_clear_cancel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import uk.gov.onelogin.sharing.sdk.api.verifier.VerificationSession
import uk.gov.onelogin.sharing.sdk.internal.verifier.VerificationSessionImpl
import uk.gov.onelogin.sharing.ui.impl.di.VerifierUiGraph
import uk.gov.onelogin.sharing.verifier.MonitorVerifierSessionState
import uk.gov.onelogin.sharing.verifier.VerifierRoutes
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes
import uk.gov.onelogin.sharing.verifier.cancellation.dialog.VerifierCancellationDialogNavigationExt.navigateToVerifierUserCancellationDialog

/**
 * Composable entry point for the Verifier role (credential verification) using the new
 * session-based API.
 *
 * The SDK internally manages the UI graph lifecycle. The consumer is responsible for
 * caching the [VerificationSession] across configuration changes (e.g., in a ViewModel).
 * The internal UI graph is recreated after configuration changes but the orchestrator
 * state (including BLE connections) is preserved.
 *
 * @param session The [VerificationSession] created via [VerifyCredentialSdk.createSession].
 * @param modifier Optional [Modifier] to apply to the root composable.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VerifyCredential(session: VerificationSession, modifier: Modifier = Modifier) {
    val sessionImpl = session as VerificationSessionImpl
    val uiGraph = remember(sessionImpl.appGraph, sessionImpl.orchestrator) {
        createGraphFactory<VerifierUiGraph.Factory>()
            .create(sessionImpl.appGraph, sessionImpl.orchestrator)
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

/**
 * Composable entry point for the Verifier role (credential verification).
 *
 * Renders the complete Verifier UI flow including camera scanning and navigation,
 * allowing the app to request and verify credentials from holders.
 *
 * @param component The [CredentialVerifier] containing the app graph and verification request.
 * @param modifier Optional [Modifier] to apply to the root composable.
 * @deprecated Use [VerifyCredential] with a [VerificationSession] instead.
 */
@Deprecated(
    message = "Use VerifyCredential(session: VerificationSession) instead.",
    replaceWith = ReplaceWith("VerifyCredential(session, modifier)")
)
@Suppress("DEPRECATION")
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
    val state: VerifierSessionState by verifierSessionState.collectAsStateWithLifecycle()

    val onCancel: () -> Unit = {
        if (state.shouldConfirmCancellation()) {
            controller.navigateToVerifierUserCancellationDialog()
        } else {
            orchestrator.cancel()
        }
    }

    MonitorVerifierSessionState(
        sessionState = verifierSessionState,
        controller = controller
    )

    BackHandler(state.userCanCancel(), onBack = onCancel)

    val scope = rememberCoroutineScope { defaultDispatcher }
    LaunchedEffect(Unit) {
        scope.launch {
            if (verifierSessionState.value is VerifierSessionState.NotStarted) {
                orchestrator.start()
            }
        }
    }

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides viewModelFactory
    ) {
        Surface(modifier = modifier) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!state.isComplete()) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            painter = painterResource(ic_menu_close_clear_cancel),
                            contentDescription = "Close"
                        )
                    }
                }
                NavHost(
                    navController = controller,
                    startDestination = VerifierRoutes,
                    modifier = Modifier.fillMaxSize()
                ) {
                    configureVerifierRoutes(controller)
                }
            }
        }
    }
}
