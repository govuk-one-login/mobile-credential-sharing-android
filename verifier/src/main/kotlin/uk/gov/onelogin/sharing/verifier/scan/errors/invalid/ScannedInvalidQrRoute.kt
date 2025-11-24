package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data class ScannedInvalidQrRoute(val data: String) {
    companion object {
        fun NavGraphBuilder.scannedInvalidQrErrorRoute(onTryAgainClick: () -> Unit = {}) {
            composable<ScannedInvalidQrRoute> { navBackStackEntry ->
                val arguments: ScannedInvalidQrRoute = navBackStackEntry.toRoute()
                ScannedInvalidQrScreen(
                    inputUri = arguments.data,
                    onTryAgainClick = onTryAgainClick
                )
            }
        }
    }
}
