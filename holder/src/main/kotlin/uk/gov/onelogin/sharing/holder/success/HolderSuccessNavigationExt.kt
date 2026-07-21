package uk.gov.onelogin.sharing.holder.success

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object HolderSuccessNavigationExt {
    fun NavController.navigateToHolderSuccessScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderSuccessRoute, options)

    internal fun NavGraphBuilder.configureHolderSuccessScreen() {
        composable<HolderSuccessRoute> {
            HolderSuccessScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            )
        }
    }
}
