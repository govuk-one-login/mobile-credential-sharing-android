package uk.gov.onelogin.sharing.core.presentation.bluetooth

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class BtConnectionErrorRoute(val title: String) {

    companion object {
        @OptIn(ExperimentalPermissionsApi::class)
        fun NavGraphBuilder.configureBluetoothConnectionErrorRoute(controller: NavController) {
            composable<BtConnectionErrorRoute> { navBackstackEntry ->
                val arguments: BtConnectionErrorRoute = navBackstackEntry.toRoute()

                BluetoothConnectionErrorScreen(
                    title = arguments.title,
                    onTryAgainClick = controller::popBackStack
                )
            }
        }
    }
}
