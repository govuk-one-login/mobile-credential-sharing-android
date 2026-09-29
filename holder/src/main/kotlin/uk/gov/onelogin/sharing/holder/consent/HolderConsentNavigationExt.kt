package uk.gov.onelogin.sharing.holder.consent

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object HolderConsentNavigationExt {
    fun NavController.navigateToHolderConsentScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderConsentRoute, options)

    internal fun NavGraphBuilder.configureHolderConsentScreen() {
        composable<HolderConsentRoute> {
            HolderConsentScreen()
        }
    }
}
