package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.api.CredentialSharingUi
import uk.gov.onelogin.sharing.ui.impl.HolderHomeRoute.configureHolderWelcomeScreen
import uk.gov.onelogin.sharing.ui.impl.VerifyCredentialRoute.configureVerifyCredentialRoute
import uk.gov.onelogin.sharing.ui.impl.di.CredentialSharingUiGraph
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanFromRoot
import uk.gov.onelogin.sharing.verifier.verify.VerifyCredentialScreen

/**
 * Implementation of [CredentialSharingUi] that provides the entry point for the
 * Credential Sharing UI.
 *
 * This class handles the creation of the UI dependency graph and sets up
 * the [LocalMetroViewModelFactory] for the UI components.
 */
class CredentialSharingUiImpl : CredentialSharingUi {
    /**
     * Renders the Credential Sharing UI.
     *
     * @param sdk The [CredentialSharingSdk] instance to use for core functionality.
     * @param startDestination The initial screen to display in the Credential Sharing flow.
     * @param modifier The [Modifier] to apply to the UI.
     */
    @Composable
    override fun Render(
        sdk: CredentialSharingSdk,
        startDestination: CredentialSharingDestination,
        modifier: Modifier
    ) {
        val uiGraph = remember(sdk.appGraph) {
            createGraphFactory<CredentialSharingUiGraph.Factory>()
                .create(sdk.appGraph)
        }

        CompositionLocalProvider(
            LocalMetroViewModelFactory provides uiGraph.metroViewModelFactory
        ) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = modifier
            ) {

                configureHolderWelcomeScreen()

                configureVerifyCredentialRoute(
                    navController = navController
                )

            }
        }
    }
}

@Serializable
object HolderHomeRoute {
    fun NavGraphBuilder.configureHolderWelcomeScreen() {

        composable<CredentialSharingDestination.HolderRoot> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HolderWelcomeScreen()
            }
        }
    }
}

@Serializable
object VerifyCredentialRoute {
    /**
     * [NavGraphBuilder] extension function for configuring the [VerifyCredentialRoute] navigation
     * target.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.configureVerifyCredentialRoute(
        navController: NavController,
    ) {

        configureVerifierRoutes(navController)

        composable<CredentialSharingDestination.VerifierRoot> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerifyCredentialScreen(
                    navigateToScanner = { navController.navigateToVerifierScanFromRoot() }
                )
            }
        }
    }
}
