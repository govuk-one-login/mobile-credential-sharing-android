package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.impl.CredentialSharingRoutes.configureCredentialSharingRoutes

@Composable
fun CredentialSharingUiNavHost(
    startDestination: CredentialSharingDestination,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        configureCredentialSharingRoutes(navController)
    }
}
