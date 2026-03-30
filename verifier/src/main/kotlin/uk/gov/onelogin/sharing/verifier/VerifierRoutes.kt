package uk.gov.onelogin.sharing.verifier

import androidx.annotation.Keep
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceNavigationExt.configureConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceNavigationExt.navigateToConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute.Companion.configureBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute.Companion.navigateToBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.configureVerifierScannerRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.navigateToScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesNavigationExt.configureVerifierPrerequisitesRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesRoute

/**
 * Serializable data object that acts as a navigation route for the Wallet sharing verifier module.
 */
@Keep
@Serializable
data object VerifierRoutes {

    /**
     * [NavGraphBuilder] extension function that configures a
     * [Nested navigation graph](https://developer.android.com/guide/navigation/design/nested-graphs#compose)
     * for the Verifier's journey for validating digital credentials.
     *
     * See also:
     * - The
     *   [User journey diagram](https://github.com/govuk-one-login/mobile-credential-sharing-android/tree/main/verifier/doc/verifierUserJourney.mmd)
     *   for a visualisation aid.
     *
     * @see configureVerifierScannerRoute
     * @see configureScannedInvalidQrRoute
     */
    fun NavGraphBuilder.configureVerifierRoutes(navController: NavHostController) {
        navigation<VerifierRoutes>(startDestination = VerifierPrerequisitesRoute) {
            configureVerifierPrerequisitesRoute(navController)
            configureVerifierScannerRoute(
                onInvalidBarcode = {
                    navController.navigateToScannedInvalidQrRoute(uri = it)
                },
                onValidBarcode = {
                    navController.navigateToConnectWithHolderDeviceRoute()
                }
            )
            configureScannedInvalidQrRoute(
                onTryAgainClick = {
                    navController.navigate(VerifierPrerequisitesRoute) {
                        popUpTo<VerifierRoutes> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
            configureConnectWithHolderDeviceRoute {
                navController.navigateToBluetoothConnectionErrorRoute(title = it)
            }
            configureBluetoothConnectionErrorRoute(controller = navController)
        }
    }
}
