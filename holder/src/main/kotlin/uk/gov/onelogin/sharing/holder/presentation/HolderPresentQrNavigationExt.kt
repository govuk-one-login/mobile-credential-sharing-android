package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object HolderPresentQrNavigationExt {
    fun NavController.navigateToHolderPresentQrScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderPresentQrRoute, options)

    internal fun NavGraphBuilder.configureHolderPresentQrScreen() {
        composable<HolderPresentQrRoute> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HolderWelcomeScreen()
            }
        }
    }
}
