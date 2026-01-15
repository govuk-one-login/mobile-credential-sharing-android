package uk.gov.onelogin.sharing.verifier.connect

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.verifier.connect.error.errorTitle
import uk.gov.onelogin.sharing.verifier.di.VerifierGraph
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
data class ConnectWithHolderDeviceRoute(val base64EncodedEngagement: String) {
    companion object {
        /**
         * [NavGraphBuilder] extension function for configuring a work-in-progress navigation
         * target.
         */
        @OptIn(ExperimentalPermissionsApi::class)
        fun NavGraphBuilder.configureConnectWithHolderDeviceRoute(
            context: Context,
            onFindError: (String) -> Unit = {}
        ) {
            val graph = createGraphFactory<VerifierGraph.Factory>().create(
                context
            )

            composable<ConnectWithHolderDeviceRoute> { navBackstackEntry ->
                val arguments: ConnectWithHolderDeviceRoute = navBackstackEntry.toRoute()

                CompositionLocalProvider(
                    LocalMetroViewModelFactory provides graph.metroViewModelFactory
                ) {
                    ConnectWithHolderDeviceScreen(
                        base64EncodedEngagement = arguments.base64EncodedEngagement,
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
