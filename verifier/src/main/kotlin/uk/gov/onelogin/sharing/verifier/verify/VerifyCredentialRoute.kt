package uk.gov.onelogin.sharing.verifier.verify

import android.content.Context
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verifier.di.VerifierGraph

/**
 * Serialization object used as a navigation route. Maps to the [VerifyCredential] composable UI.
 */
@Keep
@Serializable
object VerifyCredentialRoute {

    /**
     * [NavGraphBuilder] extension function for configuring the [VerifyCredentialRoute] navigation
     * target.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.configureVerifyCredentialRoute(context: Context) {
        val graph = createGraphFactory<VerifierGraph.Factory>().create(
            context
        )

        composable<VerifyCredentialRoute> {
            CompositionLocalProvider(
                LocalMetroViewModelFactory provides graph.metroViewModelFactory
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VerifyCredential()
                }
            }
        }
    }
}
