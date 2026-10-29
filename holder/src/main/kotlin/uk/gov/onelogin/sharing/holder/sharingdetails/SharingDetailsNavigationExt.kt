package uk.gov.onelogin.sharing.holder.sharingdetails

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object SharingDetailsNavigationExt {
    fun NavController.navigateToSharingDetailsScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(SharingDetailsRoute, options)

    internal fun NavGraphBuilder.configureSharingDetailsScreen() {
        composable<SharingDetailsRoute> {
            SharingDetailsScreen()
        }
    }
}
