package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object BluetoothPermissionDeniedNavigationExt {
    fun NavController.navigateToBluetoothPermissionDenied(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(BluetoothPermissionDeniedRoute, options)

    internal fun NavGraphBuilder.configureBluetoothPermissionDenied() {
        composable<BluetoothPermissionDeniedRoute> {
            val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
            BluetoothPermissionDeniedScreen(
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
