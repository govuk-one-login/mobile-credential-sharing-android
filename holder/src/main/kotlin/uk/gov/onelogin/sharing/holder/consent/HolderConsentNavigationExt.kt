package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object HolderConsentNavigationExt {
    fun NavController.navigateToHolderConsentScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderConsentRoute, options)

    @OptIn(ExperimentalMaterial3Api::class)
    internal fun NavGraphBuilder.configureHolderConsentScreen() {
        composable<HolderConsentRoute> {
            HolderConsentScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
