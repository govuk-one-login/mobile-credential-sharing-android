package uk.gov.onelogin.sharing.holder

import androidx.navigation.NavGraphBuilder
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute.configureHolderWelcomeScreen

object HolderRoutes {
    fun NavGraphBuilder.configureHolderRoutes(appGraph: CredentialSharingAppGraph) {
        configureHolderWelcomeScreen(appGraph)
    }
}
