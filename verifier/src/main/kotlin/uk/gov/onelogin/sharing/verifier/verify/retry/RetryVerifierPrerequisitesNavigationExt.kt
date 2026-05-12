package uk.gov.onelogin.sharing.verifier.verify.retry

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object RetryVerifierPrerequisitesNavigationExt {
    fun NavController.navigateToRetryVerifierPrerequisites(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(RetryVerifierPrerequisitesRoute, options)

    internal fun NavGraphBuilder.configureRetryVerifierPrerequisites() {
        composable<RetryVerifierPrerequisitesRoute> {
            RetryVerifierPrerequisitesScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
