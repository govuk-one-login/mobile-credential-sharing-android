package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog

object MoreAboutDvsNavigationExt {
    fun NavController.navigateToMoreAboutDvs(
        options: NavOptionsBuilder.() -> Unit
    ) = navigate(
        MoreAboutDvsRoute,
        options
    )

    internal fun NavGraphBuilder.configureMoreAboutDvsScreen() {
        dialog<MoreAboutDvsRoute>(
            dialogProperties = DialogProperties(
                decorFitsSystemWindows = false
            )
        ) {
            MoreAboutDvsScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}