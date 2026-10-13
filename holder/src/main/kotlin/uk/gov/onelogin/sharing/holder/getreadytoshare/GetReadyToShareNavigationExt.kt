package uk.gov.onelogin.sharing.holder.getreadytoshare

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object GetReadyToShareNavigationExt {
    fun NavController.navigateToGetReadyToShare(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(GetReadyToShareRoute, options)

    internal fun NavGraphBuilder.configureGetReadyToShare() {
        composable<GetReadyToShareRoute> {
            GetReadyToShareScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
