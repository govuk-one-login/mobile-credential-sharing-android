package uk.gov.onelogin.sharing.holder.error

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UnrecoverableHolderErrorNavigationExt {
    fun NavController.navigateToUnrecoverableHolderError(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(UnrecoverableHolderErrorRoute, options)

    internal fun NavGraphBuilder.configureUnrecoverableHolderError() {
        composable<UnrecoverableHolderErrorRoute> {
            val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
            val scope = rememberCoroutineScope { Dispatchers.Main }
            UnrecoverableHolderErrorScreen(
                onExitJourney = {
                    scope.launch {
                        onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            )
        }
    }
}
