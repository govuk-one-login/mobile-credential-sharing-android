package uk.gov.onelogin.sharing.verifier

import androidx.annotation.Keep
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.configureConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.navigateToConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute.Companion.configureBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute.Companion.navigateToBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.configureVerifierScannerRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.navigateToScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifyCredentialRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifyCredentialRoute.configureVerifyCredentialRoute

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
        navigation<VerifierRoutes>(startDestination = VerifyCredentialRoute) {
            configureVerifyCredentialRoute(
                navController
            )
            configureVerifierScannerRoute(
                onInvalidBarcode = {
                    navController.navigateToScannedInvalidQrRoute(uri = it)
                },
                onValidBarcode = {
                    navController.navigateToConnectWithHolderDeviceRoute(uri = it)
                }
            )
            configureScannedInvalidQrRoute(
                onTryAgainClick = { navController.navigateToVerifierScanRoute() }
            )
            configureConnectWithHolderDeviceRoute {
                navController.navigateToBluetoothConnectionErrorRoute(title = it)
            }
            configureBluetoothConnectionErrorRoute(controller = navController)
        }
    }
}
