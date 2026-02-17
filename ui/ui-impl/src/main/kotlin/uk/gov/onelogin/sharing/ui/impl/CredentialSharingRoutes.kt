package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.impl.dev.DevMenuScreen
import uk.gov.onelogin.sharing.verifier.VerifierRoutes
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes

object CredentialSharingRoutes {

    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.configureCredentialSharingRoutes(navController: NavHostController) {
        configureHolderRoutes()

        configureVerifierRoutes(navController)

        // immediately navigate to the holder root screen
        composable<CredentialSharingDestination.HolderRoot> {
            LaunchedEffect(Unit) {
                navController.navigate(HolderHomeRoute) {
                    popUpTo(CredentialSharingDestination.HolderRoot) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        // immediately navigate to the verifier root screen
        composable<CredentialSharingDestination.VerifierRoot> {
            LaunchedEffect(Unit) {
                navController.navigate(VerifierRoutes) {
                    popUpTo(CredentialSharingDestination.VerifierRoot) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        composable<CredentialSharingDestination.DevMenu> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 64.dp)
            ) {
                DevMenuScreen()
            }
        }
    }
}
