package uk.gov.onelogin.sharing.verifier.connect

import android.util.Log
import androidx.annotation.Keep
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.verifier.connect.error.errorTitle
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
object ConnectWithHolderDeviceRoute {
    /**
     * [NavGraphBuilder] extension function for configuring a work-in-progress navigation
     * target.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.configureConnectWithHolderDeviceRoute(onFindError: (String) -> Unit = {}) {
        composable<ConnectWithHolderDeviceRoute> {
            val context = LocalContext.current
            ConnectWithHolderDeviceScreen(
                onConnectionError = { error: ConnectWithHolderDeviceError ->
                    errorTitle(context, error)
                        .let(onFindError::invoke)
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

    fun NavController.navigateToConnectWithHolderDeviceRoute() = navigate(
        ConnectWithHolderDeviceRoute
    ) {
        popUpTo<VerifierScanRoute> {
            inclusive = true
        }
    }
}
