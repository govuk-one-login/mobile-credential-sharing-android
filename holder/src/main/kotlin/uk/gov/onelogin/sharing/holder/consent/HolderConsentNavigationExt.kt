package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.navigateToUnrecoverableHolderError

object HolderConsentNavigationExt {
    fun NavController.navigateToHolderConsentScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderConsentRoute, options)

    internal fun NavGraphBuilder.configureHolderConsentScreen(controller: NavController) {
        composable<HolderConsentRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }
            HolderConsentScreen {
                scope.launch {
                    controller.navigateToUnrecoverableHolderError()
                }
            }
        }
    }
}
