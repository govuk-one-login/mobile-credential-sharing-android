package uk.gov.onelogin.sharing.verifier.error

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UnrecoverableVerifierErrorNavigationExt {
    fun NavController.navigateToUnrecoverableVerifierError(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(UnrecoverableVerifierErrorRoute, options)

    internal fun NavGraphBuilder.configureUnrecoverableVerifierError() {
        composable<UnrecoverableVerifierErrorRoute> {
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
            val scope = rememberCoroutineScope { Dispatchers.Main }
            UnrecoverableVerifierErrorScreen(
                onExitJourney = {
                    scope.launch {
                        backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            )
        }
    }
}
