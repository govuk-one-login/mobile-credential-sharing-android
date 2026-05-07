package uk.gov.onelogin.sharing.holder.prerequisites.retry

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object RetryHolderPrerequisitesNavigationExt {
    fun NavController.navigateToRetryHolderPrerequisites(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(RetryHolderPrerequisitesRoute, options)

    internal fun NavGraphBuilder.configureRetryHolderPrerequisites() {
        composable<RetryHolderPrerequisitesRoute> {
            RetryHolderPrerequisitesScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
