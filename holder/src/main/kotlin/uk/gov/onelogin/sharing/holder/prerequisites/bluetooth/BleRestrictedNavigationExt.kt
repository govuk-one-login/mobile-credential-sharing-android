package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object BleRestrictedNavigationExt {
    fun NavController.navigateToBleRestricted(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(BleRestrictedRoute, options)

    internal fun NavGraphBuilder.configureBleRestricted() {
        composable<BleRestrictedRoute> {
            val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
            BleRestrictedScreen(
                modifier = Modifier.fillMaxSize(),
                onGoBackToDocuments = {
                    backPressDispatcher
                        ?.onBackPressedDispatcher
                        ?.onBackPressed()
                }
            )
        }
    }
}
