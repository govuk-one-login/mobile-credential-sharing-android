package uk.gov.onelogin.sharing.holder.prerequisites.retry

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.navigateToUnrecoverableHolderError
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.navigateToHolderPresentQrScreen

object RetryHolderPrerequisitesNavigationExt {
    fun NavController.navigateToRetryHolderPrerequisites(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(RetryHolderPrerequisitesRoute, options)

    internal fun NavGraphBuilder.configureRetryHolderPrerequisites(controller: NavController) {
        composable<RetryHolderPrerequisitesRoute> {
            RetryHolderPrerequisitesScreen(
                modifier = Modifier.fillMaxSize(),
                onPassPrerequisites = {
                    controller.navigateToHolderPresentQrScreen()
                },
                onUnrecoverableError = {
                    controller.navigateToUnrecoverableHolderError()
                }
            )
        }
    }
}
