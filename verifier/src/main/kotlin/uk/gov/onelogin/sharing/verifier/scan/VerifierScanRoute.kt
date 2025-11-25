package uk.gov.onelogin.sharing.verifier.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.serialization.Serializable

/**
 * Serialization object used as a navigation route. Maps to the [VerifierScanner] composable UI.
 */
@Serializable
object VerifierScanRoute {

    /**
     * [NavGraphBuilder] extension function for configuring the [VerifierScanRoute] navigation
     * target.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.configureVerifierScannerRoute() {
        composable<VerifierScanRoute> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerifierScanner()
            }
        }
    }
}
