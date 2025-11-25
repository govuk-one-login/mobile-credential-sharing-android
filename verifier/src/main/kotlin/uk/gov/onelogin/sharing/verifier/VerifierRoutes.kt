package uk.gov.onelogin.sharing.verifier

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.configureVerifierScannerRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.scannedInvalidQrErrorRoute

/**
 * Serializable data object that acts as a navigation route for the Wallet sharing verifier module.
 */
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
     * @see scannedInvalidQrErrorRoute
     */
    fun NavGraphBuilder.configureVerifierRoutes(
        onNavigate: (Any, NavOptionsBuilder.() -> Unit) -> Unit = { _, _ -> }
    ) {
        navigation<VerifierRoutes>(startDestination = VerifierScanRoute) {
            configureVerifierScannerRoute(
                onInvalidBarcode = { invalidBarcodeUri ->
                    onNavigate(ScannedInvalidQrRoute(data = invalidBarcodeUri)) {}
                },
                onValidBarcode = { _ ->
                    // DCMAW-16278: Navigate to empty screen
                }
            )
            scannedInvalidQrErrorRoute(
                onTryAgainClick = {
                    onNavigate(VerifierScanRoute) {
                        popUpTo<VerifierScanRoute> {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
