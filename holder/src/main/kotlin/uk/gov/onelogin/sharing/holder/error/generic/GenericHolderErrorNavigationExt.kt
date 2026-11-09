package uk.gov.onelogin.sharing.holder.error.generic

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorRoute
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorScreen

object GenericHolderErrorNavigationExt {
    fun NavController.navigateToGenericHolderError(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(UnrecoverableHolderErrorRoute, options)

    internal fun NavGraphBuilder.configureGenericHolderError() {
        composable<GenericHolderErrorRoute> {
            val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
            val scope = rememberCoroutineScope { Dispatchers.Main }
            GenericHolderErrorScreen(
                onExitJourney = {
                    scope.launch {
                        onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            )
        }
    }
}
