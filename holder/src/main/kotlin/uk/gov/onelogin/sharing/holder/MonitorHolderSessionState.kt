package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.core.presentation.bluetooth.errorTitle
import uk.gov.onelogin.sharing.holder.HolderNavigationExtensions.navigateToBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.navigateToHolderConsentScreen
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.navigateToUnrecoverableHolderError
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesNavigationExt.navigateToHolderPrerequisitesScreen
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesNavigationExt.navigateToRetryHolderPrerequisites
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.navigateToHolderPresentQrScreen
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

/**
 * Creates a [LaunchedEffect] that monitors the [holderSessionState].
 *
 * These states map to [NavHostController] extension functions, controlling the screen displayed
 * to the User.
 *
 * @see convertSessionStateToNavigation
 */
@Composable
fun MonitorHolderSessionState(
    holderSessionState: StateFlow<HolderSessionState>,
    navController: NavHostController,
    context: Context = LocalContext.current
) {
    val coroutineScope = rememberCoroutineScope { Dispatchers.Main }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            holderSessionState.map { state ->
                convertSessionStateToNavigation(
                    context,
                    navController,
                    state
                )
            }.collect { navigationFunction ->
                navigationFunction()
            }
        }
    }
}

/**
 * Converts the provided [state] into a navigation action by wrapping a [NavHostController]
 * extension function.
 *
 * Using this function tightly binds app navigation to the [HolderSessionState]. As in,
 * changes to the [state] update the current location of the User withing the holder app journey.
 *
 * The reason for doing this is due to the [HolderSessionState] acting as the User's current
 * position within the journey as a form of state machine.
 *
 * @return An anonymous function that calls [NavHostController] extension functions. Invoke the
 * returned value to perform navigation.
 */
internal suspend fun convertSessionStateToNavigation(
    context: Context,
    navController: NavHostController,
    state: HolderSessionState,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
): () -> Unit = withContext(dispatcher) {
    when (state) {
        HolderSessionState.NotStarted -> {
            {
                navController.navigateToHolderPrerequisitesScreen {
                    popUpTo<HolderRoutes> {
                        inclusive = true
                    }
                }
            }
        }

        is HolderSessionState.Preflight -> {
            {
                navController.navigateToRetryHolderPrerequisites {
                    popUpTo<HolderRoutes> {
                        inclusive = true
                    }
                }
            }
        }

        is HolderSessionState.PresentingEngagement -> {
            {
                navController.navigateToHolderPresentQrScreen {
                    popUpTo<HolderRoutes> {
                        inclusive = true
                    }
                }
            }
        }

        is HolderSessionState.AwaitingUserConsent -> {
            {
                navController.navigateToHolderConsentScreen()
            }
        }

        is HolderSessionState.Complete.Failed -> {
            {
                handleHolderSessionFailure(state, navController, context)
            }
        }

        HolderSessionState.ReadyToPresent,
        HolderSessionState.ProcessingEstablishment,
        HolderSessionState.ProcessingResponse,
        HolderSessionState.AwaitingVerifierResolution,
        HolderSessionState.Complete.Cancelled,
        is HolderSessionState.Complete.Success
        -> {
            {
                // do nothing with unrelated / unimplemented states
            }
        }
    }
}

private fun handleHolderSessionFailure(
    state: HolderSessionState.Complete.Failed,
    navController: NavHostController,
    context: Context
) {
    if ((state.sessionReason as? SessionErrorReason.UnrecoverableThrowable)
            ?.exception is BluetoothDisconnectedException
    ) {
        navController.navigateToBluetoothConnectionErrorRoute(
            errorTitle(
                context,
                BluetoothSessionError.BluetoothConnectionError
            )
        )
    } else {
        navController.navigateToUnrecoverableHolderError {
            popUpTo<HolderRoutes> {
                inclusive = true
            }
        }
    }
}
