package uk.gov.onelogin.sharing.verifier.verify

import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.verifier.di.VerifierGraph
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanFromRoot

/**
 * Serialization object used as a navigation route. Maps to the [VerifyCredentialScreen] composable UI.
 */
@Keep
@Serializable
object VerifyCredentialRoute {

    /**
     * [NavGraphBuilder] extension function for configuring the [VerifyCredentialRoute] navigation
     * target.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.configureVerifyCredentialRoute(
        navController: NavController,
        appGraph: CredentialSharingAppGraph
    ) {
        composable<VerifyCredentialRoute> {
            val graph = remember {
                createGraphFactory<VerifierGraph.Factory>().create(
                    appGraph
                )
            }

            CompositionLocalProvider(
                LocalMetroViewModelFactory provides graph.metroViewModelFactory
            ) {
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
}
