package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.verifier.VerifierRoutes
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes

object CredentialSharingRoutes {

    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.configureCredentialSharingRoutes(navController: NavHostController) {
        configureHolderRoutes()

        configureVerifierRoutes(navController)

        // immediately navigate to the holder root screen
        composable<CredentialSharingDestination.Holder> {
            LaunchedEffect(Unit) {
                navController.navigate(HolderHomeRoute) {
                    popUpTo(CredentialSharingDestination.Holder) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        // immediately navigate to the verifier root screen
        composable<CredentialSharingDestination.Verifier> {
            LaunchedEffect(Unit) {
                navController.navigate(VerifierRoutes) {
                    popUpTo(CredentialSharingDestination.Verifier) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
}
