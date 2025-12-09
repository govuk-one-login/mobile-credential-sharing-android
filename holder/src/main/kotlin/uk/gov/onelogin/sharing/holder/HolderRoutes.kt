package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.navigation.NavGraphBuilder
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute.configureHolderWelcomeScreen

object HolderRoutes {
    fun NavGraphBuilder.configureHolderRoutes(context: Context) {
        configureHolderWelcomeScreen(context)
    }
}
