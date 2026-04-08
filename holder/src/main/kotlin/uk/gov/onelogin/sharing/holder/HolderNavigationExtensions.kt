package uk.gov.onelogin.sharing.holder

import androidx.navigation.NavController
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BtConnectionErrorRoute

object HolderNavigationExtensions {

    fun NavController.navigateToBluetoothConnectionErrorRoute(title: String) =
        navigate(BtConnectionErrorRoute(title)) {
            popUpTo<HolderRoutes> {
                inclusive = false
            }
        }
}
