package uk.gov.onelogin.sharing.verifier.connect

import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.core.presentation.bluetooth.errorTitle
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
    internal fun NavGraphBuilder.configureConnectWithHolderDeviceRoute(controller: NavController) {
        composable<ConnectWithHolderDeviceRoute> {
            val context = LocalContext.current
            val scope = rememberCoroutineScope { Dispatchers.Main }
            ConnectWithHolderDeviceScreen(
                onConnectionError = { error: BluetoothSessionError ->
                    errorTitle(context, error)
                        .let {
                            scope.launch {
                                controller.navigateToBluetoothConnectionErrorRoute(title = it)
                            }
                        }
                        .also {
                            Log.w(
                                ConnectWithHolderDeviceRoute::class.java.simpleName,
                                "Navigated to error screen: $error"
                            )
                        }
                }
            )
        }
    }
}
