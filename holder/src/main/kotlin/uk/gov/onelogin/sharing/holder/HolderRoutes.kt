package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BtConnectionErrorRoute.Companion.configureBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.core.presentation.bluetooth.errorTitle
import uk.gov.onelogin.sharing.holder.HolderNavigationExtensions.navigateToBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.configureHolderConsentScreen
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.navigateToHolderConsentScreen
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.configureUnrecoverableHolderError
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.navigateToUnrecoverableHolderError
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesNavigationExt.configureHolderPrerequisitesScreen
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesNavigationExt.navigateToHolderPrerequisitesScreen
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesNavigationExt.configureRetryHolderPrerequisites
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesNavigationExt.navigateToRetryHolderPrerequisites
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.configureHolderPresentQrScreen
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.navigateToHolderPresentQrScreen
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@Keep
@Serializable
data object HolderRoutes {
    fun NavController.navigateToHolderJourney(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderRoutes, options)

    fun NavGraphBuilder.configureHolderRoutes(controller: NavController) {
        navigation<HolderRoutes>(startDestination = HolderPrerequisitesRoute) {
            configureHolderPrerequisitesScreen()
            configureUnrecoverableHolderError(controller)
            configureRetryHolderPrerequisites()
            configureHolderPresentQrScreen()
            configureHolderConsentScreen()
            configureBluetoothConnectionErrorRoute(controller)
        }
    }

    // DCMAW-19768: Unit test this function
    suspend fun convertSessionStateToNavigation(
        context: Context,
        navController: NavHostController,
        state: HolderSessionState,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): () -> Unit = withContext(dispatcher) {
        when (state) {
            HolderSessionState.NotStarted -> {
                {
                    navController.navigateToHolderPrerequisitesScreen()
                }
            }

            is HolderSessionState.Preflight -> {
                {
                    navController.navigateToRetryHolderPrerequisites()
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

            is HolderSessionState.Complete.Failed -> {
                {
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
            }

            is HolderSessionState.AwaitingUserConsent -> {
                {
                    navController.navigateToHolderConsentScreen()
                }
            }

            else -> {
                // do nothing with unrelated sessions
                {}
            }
        }
    }
}
