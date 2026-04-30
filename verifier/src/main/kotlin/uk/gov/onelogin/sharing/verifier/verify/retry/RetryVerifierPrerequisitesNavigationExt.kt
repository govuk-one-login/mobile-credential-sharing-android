package uk.gov.onelogin.sharing.verifier.verify.retry

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.verifier.error.UnrecoverableVerifierErrorNavigationExt.navigateToUnrecoverableVerifierError
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanFromRoot

object RetryVerifierPrerequisitesNavigationExt {
    fun NavController.navigateToRetryVerifierPrerequisites(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(RetryVerifierPrerequisitesRoute, options)

    internal fun NavGraphBuilder.configureRetryVerifierPrerequisites(controller: NavController) {
        composable<RetryVerifierPrerequisitesRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }

            RetryVerifierPrerequisitesScreen(
                modifier = Modifier.fillMaxSize(),
                onPassPrerequisites = {
                    scope.launch {
                        controller.navigateToVerifierScanFromRoot()
                    }
                },
                onUnrecoverableError = {
                    scope.launch {
                        controller.navigateToUnrecoverableVerifierError()
                    }
                }
            )
        }
    }
}
