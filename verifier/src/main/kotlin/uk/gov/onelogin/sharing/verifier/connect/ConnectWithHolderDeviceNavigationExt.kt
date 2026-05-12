package uk.gov.onelogin.sharing.verifier.connect

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute

object ConnectWithHolderDeviceNavigationExt {
    fun NavController.navigateToConnectWithHolderDeviceRoute() = navigate(
        ConnectWithHolderDeviceRoute
    ) {
        popUpTo<VerifierScanRoute> {
            inclusive = true
        }
    }

    fun NavController.navigateToBluetoothConnectionErrorRoute(title: String) =
        navigate(BluetoothConnectionErrorRoute(title)) {
            popUpTo<ConnectWithHolderDeviceRoute> {
                inclusive = false
            }
        }

    @OptIn(ExperimentalPermissionsApi::class)
    internal fun NavGraphBuilder.configureConnectWithHolderDeviceRoute() {
        composable<ConnectWithHolderDeviceRoute> {
            ConnectWithHolderDeviceScreen()
        }
    }
}
