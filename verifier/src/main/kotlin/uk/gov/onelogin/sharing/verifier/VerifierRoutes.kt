package uk.gov.onelogin.sharing.verifier

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verifier.cancellation.VerifierCancellationScreenNavigationExt.configureVerifierUserCancellationScreen
import uk.gov.onelogin.sharing.verifier.cancellation.dialog.VerifierCancellationDialogNavigationExt.configureVerifierUserCancellationDialog
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceNavigationExt.configureConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute.Companion.configureBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.error.UnrecoverableVerifierErrorNavigationExt.configureUnrecoverableVerifierError
import uk.gov.onelogin.sharing.verifier.finish.FinishedVerifierJourneyNavigationExt.configureFinishedVerifierJourney
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.configureVerifierScannerRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesNavigationExt.configureVerifierPrerequisitesRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesRoute
import uk.gov.onelogin.sharing.verifier.verify.retry.RetryVerifierPrerequisitesNavigationExt.configureRetryVerifierPrerequisites

/**
 * Serializable data object that acts as a navigation route for the Wallet sharing verifier module.
 */
@Keep
@Serializable
data object VerifierRoutes {

    fun NavController.navigateToVerifierJourney(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(VerifierRoutes, options)

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
            configureVerifierPrerequisitesRoute()
            configureUnrecoverableVerifierError()
            configureRetryVerifierPrerequisites()
            configureVerifierScannerRoute()
            configureScannedInvalidQrRoute(navController)
            configureConnectWithHolderDeviceRoute()
            configureBluetoothConnectionErrorRoute()
            configureFinishedVerifierJourney()
            configureVerifierUserCancellationDialog()
            configureVerifierUserCancellationScreen()
        }
    }
}
