package uk.gov.onelogin.sharing.holder.detailsshared

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object DetailsSharedNavigationExt {
    fun NavController.navigateToDetailsSharedScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(DetailsSharedRoute, options)

    internal fun NavGraphBuilder.configureDetailsSharedScreen() {
        composable<DetailsSharedRoute> {
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current

            DetailsSharedScreen(
                onExitJourney = {
                    backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
                }
            )
        }
    }
}
