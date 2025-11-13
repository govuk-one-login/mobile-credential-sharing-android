package uk.gov.onelogin.sharing.holder.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object HolderWelcomeRoute {
    fun NavGraphBuilder.configureHolderWelcomeScreen() {
        composable<HolderWelcomeRoute> {
            HolderWelcomeScreen()
        }
    }
}
