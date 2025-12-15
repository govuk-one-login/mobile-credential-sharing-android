package uk.gov.onelogin.sharing.verifier

import android.content.Context
import androidx.annotation.Keep
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.configureConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.navigateToConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.configureVerifierScannerRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.navigateToScannedInvalidQrRoute

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
    fun NavGraphBuilder.configureVerifierRoutes(
        navController: NavController, context: Context
    ) {
        navigation<VerifierRoutes>(startDestination = VerifierScanRoute) {
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
            configureConnectWithHolderDeviceRoute(context)
        }
    }
}
