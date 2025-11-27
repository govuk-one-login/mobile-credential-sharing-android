package uk.gov.onelogin.sharing.verifier.connect

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute

/**
 * Serialization object used as a navigation route.
 */
@Keep
@Serializable
@ImplementationDetail(
    ticket = "DCMAW-16955",
    description = "Successful handling of scanned QR code"
)
data class ConnectWithHolderDeviceRoute(val mdocUri: String) {
    companion object {
        /**
         * [NavGraphBuilder] extension function for configuring a work-in-progress navigation
         * target.
         */
        fun NavGraphBuilder.configureConnectWithHolderDeviceRoute() {
            composable<ConnectWithHolderDeviceRoute> { navBackstackEntry ->
                val arguments: ConnectWithHolderDeviceRoute = navBackstackEntry.toRoute()
                ConnectWithHolderDeviceScreen(mdocUri = arguments.mdocUri)
            }
        }

        fun NavController.navigateToConnectWithHolderDeviceRoute(uri: String) = navigate(
            ConnectWithHolderDeviceRoute(uri)
        ) {
            popUpTo<VerifierScanRoute> {
                inclusive = false
            }
        }
    }
}
