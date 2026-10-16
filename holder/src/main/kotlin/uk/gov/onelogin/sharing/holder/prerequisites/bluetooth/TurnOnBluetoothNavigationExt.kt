package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object TurnOnBluetoothNavigationExt {
    fun NavController.navigateToTurnOnBluetooth(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(TurnOnBluetoothRoute, options)

    internal fun NavGraphBuilder.configureTurnOnBluetooth() {
        composable<TurnOnBluetoothRoute> {
            // temp: reuse existing screen
            TurnOnBluetoothScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
