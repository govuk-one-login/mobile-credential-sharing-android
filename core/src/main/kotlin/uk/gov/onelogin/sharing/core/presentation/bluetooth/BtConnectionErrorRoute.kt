package uk.gov.onelogin.sharing.core.presentation.bluetooth

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.Keep
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class BtConnectionErrorRoute(val title: String) {

    companion object {
        @OptIn(ExperimentalPermissionsApi::class)
        fun NavGraphBuilder.configureBluetoothConnectionErrorRoute() {
            composable<BtConnectionErrorRoute> { navBackstackEntry ->
                val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
                val arguments: BtConnectionErrorRoute = navBackstackEntry.toRoute()
                val scope = rememberCoroutineScope { Dispatchers.Main }

                BluetoothConnectionErrorScreen(
                    title = arguments.title,
                    onTryAgainClick = {
                        scope.launch {
                            onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                        }
                    }
                )
            }
        }
    }
}
