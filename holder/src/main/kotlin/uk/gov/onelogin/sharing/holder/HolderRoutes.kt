package uk.gov.onelogin.sharing.holder

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BtConnectionErrorRoute.Companion.configureBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.holder.HolderNavigationExtensions.navigateToBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.configureHolderConsentScreen
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.configureUnrecoverableHolderError
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.navigateToUnrecoverableHolderError
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesNavigationExt.configureHolderPrerequisitesScreen
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesNavigationExt.configureRetryHolderPrerequisites
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.configureHolderPresentQrScreen

@Keep
@Serializable
data object HolderRoutes {
    fun NavController.navigateToHolderJourney(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderRoutes, options)

    fun NavGraphBuilder.configureHolderRoutes(controller: NavController) {
        navigation<HolderRoutes>(startDestination = HolderPrerequisitesRoute) {
            configureHolderPrerequisitesScreen(controller)
            configureUnrecoverableHolderError(controller)
            configureRetryHolderPrerequisites(controller)
            configureHolderPresentQrScreen(
                controller = controller,
                onError = {
                    controller.navigateToBluetoothConnectionErrorRoute(it)
                },
                onGenericError = {
                    controller.navigateToUnrecoverableHolderError()
                }
            )
            configureHolderConsentScreen(
                onError = {
                    controller.navigateToUnrecoverableHolderError()
                }
            )
            configureBluetoothConnectionErrorRoute(controller)
        }
    }
}
