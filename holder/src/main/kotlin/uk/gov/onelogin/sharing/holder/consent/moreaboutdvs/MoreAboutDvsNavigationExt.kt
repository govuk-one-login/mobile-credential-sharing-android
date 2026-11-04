package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object MoreAboutDvsNavigationExt {
    fun NavController.navigateToMoreAboutDvs(
        options: NavOptionsBuilder.() -> Unit
    ) = navigate(
        MoreAboutDvsRoute,
        options
    )

    internal fun NavGraphBuilder.configureMoreAboutDvsScreen() {
        composable<MoreAboutDvsRoute> {
            MoreAboutDvsScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}