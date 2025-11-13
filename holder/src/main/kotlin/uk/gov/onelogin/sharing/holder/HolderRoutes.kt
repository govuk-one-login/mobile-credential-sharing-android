package uk.gov.onelogin.sharing.holder

import androidx.navigation.NavGraphBuilder
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeRoute.configureHolderWelcomeScreen

object HolderRoutes {
    fun NavGraphBuilder.configureHolderDestinations() {
        configureHolderWelcomeScreen()
    }
}
