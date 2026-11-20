package uk.gov.onelogin.sharing.holder.error.generic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object GenericHolderErrorNavigationExt {
    fun NavController.navigateToGenericHolderError(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(GenericHolderErrorRoute, options)

    internal fun NavGraphBuilder.configureGenericHolderError() {
        composable<GenericHolderErrorRoute> {
            GenericHolderErrorScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
