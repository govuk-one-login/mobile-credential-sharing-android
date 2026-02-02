package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
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
import uk.gov.onelogin.sharing.verifier.di.VerifierGraph
import uk.gov.onelogin.sharing.verifier.verify.VerifyCredentialRoute

/**
 * Serialization object used as a navigation route. Maps to the [VerifierScanner] composable UI.
 */
@Keep
@Serializable
object VerifierScanRoute {

    /**
     * [NavGraphBuilder] extension function for configuring the [VerifierScanRoute] navigation
     * target.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.configureVerifierScannerRoute(
        context: Context,
        onInvalidBarcode: (String) -> Unit = {},
        onValidBarcode: (String) -> Unit = {}
    ) {
        composable<VerifierScanRoute> {
            val graph = remember {
                createGraphFactory<VerifierGraph.Factory>().create(
                    context
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
                    VerifierScanner(
                        onInvalidBarcode = onInvalidBarcode,
                        onValidBarcode = onValidBarcode
                    )
                }
            }
        }
    }

    fun NavController.navigateToVerifierScanRoute() = navigate(VerifierScanRoute) {
        popUpTo<VerifierScanRoute> {
            inclusive = true
        }
    }

    fun NavController.navigateToVerifierScanFromRoot() = navigate(VerifierScanRoute) {
        popUpTo<VerifyCredentialRoute> {
            inclusive = true
        }
    }
}
