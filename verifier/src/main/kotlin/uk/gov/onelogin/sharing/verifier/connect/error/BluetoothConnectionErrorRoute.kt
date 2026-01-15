package uk.gov.onelogin.sharing.verifier.connect.error

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute

@Keep
@Serializable
@ImplementationDetail(
    ticket = "DCMAW-17616",
    description = "Error handling for bluetooth connection errors"
)
data class BluetoothConnectionErrorRoute(val title: String) {
    companion object {
        /**
         * [NavGraphBuilder] extension function. Creates a navigation target for errors found when
         * attempting to decode digital credentials after scanning a QR code.
         */
        @OptIn(ExperimentalPermissionsApi::class)
        fun NavGraphBuilder.configureBluetoothConnectionErrorRoute(controller: NavController) {
            composable<BluetoothConnectionErrorRoute> { navBackstackEntry ->
                val arguments: BluetoothConnectionErrorRoute = navBackstackEntry.toRoute()

                BluetoothConnectionErrorScreen(
                    title = arguments.title,
                    onTryAgainClick = controller::popBackStack
                )
            }
        }

        fun NavController.navigateToBluetoothConnectionErrorRoute(title: String) =
            navigate(BluetoothConnectionErrorRoute(title)) {
                popUpTo<ConnectWithHolderDeviceRoute> {
                    inclusive = false
                }
            }
    }
}
