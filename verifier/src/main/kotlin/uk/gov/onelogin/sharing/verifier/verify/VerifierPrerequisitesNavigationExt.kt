package uk.gov.onelogin.sharing.verifier.verify

import android.util.Log
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verifier.error.UnrecoverableVerifierErrorNavigationExt.navigateToUnrecoverableVerifierError
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanFromRoot

/**
 * Serialization object used as a navigation route. Maps to the [VerifierPrerequisitesScreen] composable UI.
 */
@Keep
@Serializable
data object VerifierPrerequisitesNavigationExt {

    fun NavController.navigateToVerifierPrerequisitesScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(VerifierPrerequisitesRoute, options)

    /**
     * [NavGraphBuilder] extension function for configuring the [VerifierPrerequisitesRoute]
     * navigation target.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    internal fun NavGraphBuilder.configureVerifierPrerequisitesRoute(navController: NavController) {
        composable<VerifierPrerequisitesRoute> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerifierPrerequisitesScreen(
                    onNavigateToPreflight = {
                        Log.d(
                            "VerifyCredentialRoute",
                            "Called 'onNavigateToPreflight' behaviour"
                        )
                    },
                    onNavigateToScanner = { navController.navigateToVerifierScanFromRoot() },
                    onUnrecoverableError = { navController.navigateToUnrecoverableVerifierError() }
                )
            }
        }
    }
}
