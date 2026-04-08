package uk.gov.onelogin.sharing.holder.error

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.HolderRoutes.navigateToHolderJourney

object UnrecoverableHolderErrorNavigationExt {
    fun NavController.navigateToUnrecoverableHolderError(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(UnrecoverableHolderErrorRoute, options)

    internal fun NavGraphBuilder.configureUnrecoverableHolderError(navController: NavController) {
        composable<UnrecoverableHolderErrorRoute> {
            UnrecoverableHolderErrorScreen(
                onExitJourney = {
                    navController.navigateToHolderJourney {
                        popUpTo<HolderRoutes> {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
