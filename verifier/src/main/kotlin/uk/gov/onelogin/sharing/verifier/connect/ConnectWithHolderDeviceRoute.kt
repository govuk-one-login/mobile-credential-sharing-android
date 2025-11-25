package uk.gov.onelogin.sharing.verifier.connect

import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import uk.gov.android.ui.theme.spacingDouble

/**
 * Serialization object used as a navigation route.
 */
@Keep
@Serializable
data class ConnectWithHolderDeviceRoute(val mdocUri: String) {
    companion object {
        /**
         * [NavGraphBuilder] extension function for configuring a work-in-progress navigation
         * target.
         */
        fun NavGraphBuilder.configureConnectWithHolderDeviceRoute() {
            composable<ConnectWithHolderDeviceRoute> { navBackstackEntry ->
                val arguments: ConnectWithHolderDeviceRoute = navBackstackEntry.toRoute()
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(spacingDouble)
                ) {
                    Text("Successfully scanned QR code:")
                    Text(arguments.mdocUri)
                }
            }
        }
    }
}
