package uk.gov.onelogin.sharing.holder.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object HolderHomeRoute {
    fun NavGraphBuilder.configureHolderWelcomeScreen() {
        composable<HolderHomeRoute> {
            HolderWelcomeScreen()
        }
    }
}
