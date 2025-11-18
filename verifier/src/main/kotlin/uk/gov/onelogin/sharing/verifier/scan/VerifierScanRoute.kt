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

@Serializable
object VerifierScanRoute {
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
