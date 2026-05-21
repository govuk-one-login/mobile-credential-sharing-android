package uk.gov.onelogin.sharing.verifier

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.core.presentation.bluetooth.errorTitle
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceNavigationExt.navigateToBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceNavigationExt.navigateToConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.error.UnrecoverableVerifierErrorNavigationExt.navigateToUnrecoverableVerifierError
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.navigateToScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesNavigationExt.navigateToVerifierPrerequisitesScreen
import uk.gov.onelogin.sharing.verifier.verify.retry.RetryVerifierPrerequisitesNavigationExt.navigateToRetryVerifierPrerequisites

/**
 * Creates a [LaunchedEffect] that monitors the [sessionState].
 *
 * These states map to [NavHostController] extension functions, controlling the screen displayed
 * to the User.
 *
 * @see convertSessionStateToNavigation
 */
@Composable
fun MonitorVerifierSessionState(
    sessionState: StateFlow<VerifierSessionState>,
    controller: NavHostController,
    dispatcher: CoroutineContext = Dispatchers.Default
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope { Dispatchers.Main }
    val conversion: suspend (VerifierSessionState) -> (() -> Unit) = { state ->
        convertSessionStateToNavigation(
            context,
            controller,
            state
        )
    }

    LaunchedEffect(Unit) {
        sessionState
            .map(conversion)
            .distinctUntilChanged()
            .flowOn(dispatcher)
            .collect { navigationFunction ->
                coroutineScope.launch { navigationFunction() }
            }
    }
}

/**
 * Converts the provided [state] into a navigation action by wrapping a [NavHostController]
 * extension function.
 *
 * Using this function tightly binds app navigation to the [VerifierSessionState]. As in,
 * changes to the [state] update the current location of the User withing the verifier app journey.
 *
 * The reason for doing this is due to the [VerifierSessionState] acting as the User's current
 * position within the journey as a form of state machine.
 *
 * @return An anonymous function that calls [NavHostController] extension functions. Invoke the
 * returned value to perform navigation.
 */
internal suspend fun convertSessionStateToNavigation(
    context: Context,
    navController: NavHostController,
    state: VerifierSessionState,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
): () -> Unit = withContext(dispatcher) {
    when (state) {
        VerifierSessionState.NotStarted -> {
            {
                navController.navigateToVerifierPrerequisitesScreen {
                    popUpTo<VerifierRoutes> {
                        inclusive = true
                    }
                }
            }
        }

        is VerifierSessionState.Preflight -> {
            {
                navController.navigateToRetryVerifierPrerequisites {
                    popUpTo<VerifierRoutes> {
                        inclusive = true
                    }
                }
            }
        }

        VerifierSessionState.ReadyToScan -> {
            {
                navController.navigateToVerifierScanRoute {
                    popUpTo<VerifierRoutes> {
                        inclusive = true
                    }
                }
            }
        }

        VerifierSessionState.Connecting -> {
            {
                navController.navigateToConnectWithHolderDeviceRoute()
            }
        }

        is VerifierSessionState.Complete.Failed -> {
            {
                handleSessionFailure(state, navController, context)
            }
        }

        VerifierSessionState.ProcessingEngagement,
        VerifierSessionState.Verifying,
        is VerifierSessionState.Complete.Success,
        VerifierSessionState.Complete.Cancelled
        -> {
            {}
        }
    }
}

private fun handleSessionFailure(
    state: VerifierSessionState.Complete.Failed,
    navController: NavHostController,
    context: Context
) {
    when (val sessionErrorReason = state.error.reason) {
        is SessionErrorReason.UnsupportedQrCodeFormat ->
            navController.navigateToScannedInvalidQrRoute(sessionErrorReason.rawValue)

        SessionErrorReason.ServiceUuidNotFound -> {
            errorTitle(context, BluetoothSessionError.BluetoothConnectionError).let {
                navController.navigateToBluetoothConnectionErrorRoute(title = it)
            }
        }

        is SessionErrorReason.CannotSendMessage,
        is SessionErrorReason.CannotEncryptDeviceRequest,
        is SessionErrorReason.CannotBuildSessionEstablishment,
        is SessionErrorReason.MissingCryptoContext,
        is SessionErrorReason.CannotProcessEngagement,
        is SessionErrorReason.UnrecoverableThrowable,
        is SessionErrorReason.UnrecoverablePrerequisite,
        is SessionErrorReason.InvalidSessionDataPayload
        ->
            navController.navigateToUnrecoverableVerifierError {
                popUpTo<VerifierRoutes> {
                    inclusive = true
                }
            }
    }
}
