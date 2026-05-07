package uk.gov.onelogin.sharing.verifier.verify

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
    internal fun NavGraphBuilder.configureVerifierPrerequisitesRoute() {
        composable<VerifierPrerequisitesRoute> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerifierPrerequisitesScreen()
            }
        }
    }
}
