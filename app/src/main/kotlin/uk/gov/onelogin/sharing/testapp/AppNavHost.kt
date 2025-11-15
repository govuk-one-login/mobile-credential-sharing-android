package uk.gov.onelogin.sharing.testapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.testapp.PrimaryTabDestination.Companion.configureTestAppRoutes
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        configureTestAppRoutes(onNavigate = navController::navigate)
        configureHolderRoutes()
        configureVerifierRoutes()
    }
}
