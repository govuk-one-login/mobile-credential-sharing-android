package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.navigateToUnrecoverableHolderError
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesNavigationExt.navigateToRetryHolderPrerequisites
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.navigateToHolderPresentQrScreen

object HolderPrerequisitesNavigationExt {
    fun NavController.navigateToHolderPrerequisitesScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderPrerequisitesRoute, options)

    internal fun NavGraphBuilder.configureHolderPrerequisitesScreen(controller: NavController) {
        composable<HolderPrerequisitesRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }

            HolderPrerequisitesScreen(
                onHandlePreflight = {
                    scope.launch {
                        controller.navigateToRetryHolderPrerequisites()
                    }
                },
                onPresentEngagement = {
                    scope.launch {
                        controller.navigateToHolderPresentQrScreen {
                            popUpTo<HolderRoutes> {
                                inclusive = true
                            }
                        }
                    }
                },
                onUnrecoverableError = {
                    scope.launch {
                        controller.navigateToUnrecoverableHolderError {
                            popUpTo<HolderRoutes> {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }
    }
}
