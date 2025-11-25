package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute

/**
 * Serialization data class used as a navigation route. Maps to the [ScannedInvalidQrScreen]
 * composable UI.
 *
 * @param data The URI stored within a successfully scanned QR code.
 */
@Serializable
data class ScannedInvalidQrRoute(val data: String) {
    companion object {
        /**
         * [NavGraphBuilder] extension function for configuring the [ScannedInvalidQrRoute]
         * navigation target.
         */
        fun NavGraphBuilder.scannedInvalidQrErrorRoute(onTryAgainClick: () -> Unit = {}) {
            composable<ScannedInvalidQrRoute> { navBackStackEntry ->
                val arguments: ScannedInvalidQrRoute = navBackStackEntry.toRoute()
                ScannedInvalidQrScreen(
                    inputUri = arguments.data,
                    onTryAgainClick = onTryAgainClick
                )
            }
        }
    }
}
