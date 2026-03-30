package uk.gov.onelogin.sharing.holder

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.configureHolderConsentScreen
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesNavigationExt.configureHolderPrerequisitesScreen
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.configureHolderPresentQrScreen

@Keep
@Serializable
data object HolderRoutes {
    fun NavGraphBuilder.configureHolderRoutes(controller: NavController) {
        navigation<HolderRoutes>(startDestination = HolderPrerequisitesRoute) {
            configureHolderPrerequisitesScreen(controller)
            configureHolderPresentQrScreen(controller)
            configureHolderConsentScreen()
        }
    }
}
