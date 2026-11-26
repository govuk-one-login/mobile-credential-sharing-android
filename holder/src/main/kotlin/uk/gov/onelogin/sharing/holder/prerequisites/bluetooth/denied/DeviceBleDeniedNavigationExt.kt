package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object DeviceBleDeniedNavigationExt {
    fun NavController.navigateToDeviceBleDenied(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(DeviceBleDeniedRoute, options)

    internal fun NavGraphBuilder.configureDeviceBleDenied() {
        composable<DeviceBleDeniedRoute> {
            val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
            DeviceBleDeniedScreen(
                modifier = Modifier.fillMaxSize(),
                onCancel = {
                    backPressDispatcher
                        ?.onBackPressedDispatcher
                        ?.onBackPressed()
                }
            )
        }
    }
}
