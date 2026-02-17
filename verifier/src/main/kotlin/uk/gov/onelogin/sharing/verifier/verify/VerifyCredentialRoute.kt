package uk.gov.onelogin.sharing.verifier.verify

import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.serialization.Serializable
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
    fun NavGraphBuilder.configureVerifyCredentialRoute(navController: NavController) {
        composable<VerifyCredentialRoute> {
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
