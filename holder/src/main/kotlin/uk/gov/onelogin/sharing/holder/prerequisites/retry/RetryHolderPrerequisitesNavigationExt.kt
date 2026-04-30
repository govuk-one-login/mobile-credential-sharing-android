package uk.gov.onelogin.sharing.holder.prerequisites.retry

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.navigateToUnrecoverableHolderError
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.navigateToHolderPresentQrScreen

object RetryHolderPrerequisitesNavigationExt {
    fun NavController.navigateToRetryHolderPrerequisites(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(RetryHolderPrerequisitesRoute, options)

    internal fun NavGraphBuilder.configureRetryHolderPrerequisites(controller: NavController) {
        composable<RetryHolderPrerequisitesRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }

            RetryHolderPrerequisitesScreen(
                modifier = Modifier.fillMaxSize(),
                onPassPrerequisites = {
                    scope.launch {
                        controller.navigateToHolderPresentQrScreen()
                    }
                },
                onUnrecoverableError = {
                    scope.launch {
                        controller.navigateToUnrecoverableHolderError()
                    }
                }
            )
        }
    }
}
