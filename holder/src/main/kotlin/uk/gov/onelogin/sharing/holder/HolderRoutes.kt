package uk.gov.onelogin.sharing.holder

import androidx.annotation.Keep
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute.configureHolderWelcomeScreen

@Keep
@Serializable
data object HolderRoutes {
    fun NavGraphBuilder.configureHolderRoutes() {
        navigation<HolderRoutes>(startDestination = HolderHomeRoute) {
            configureHolderWelcomeScreen()
        }
    }
}
