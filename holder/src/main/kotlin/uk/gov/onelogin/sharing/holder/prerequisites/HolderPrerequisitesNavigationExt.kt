package uk.gov.onelogin.sharing.holder.prerequisites

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.navigateToHolderPresentQrScreen

object HolderPrerequisitesNavigationExt {
    fun NavController.navigateToHolderPrerequisitesScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderPrerequisitesRoute, options)

    internal fun NavGraphBuilder.configureHolderPrerequisitesScreen(controller: NavController) {
        composable<HolderPrerequisitesRoute> {
            val tag = "configureHolderPrerequisitesScreen"
            HolderPrerequisitesScreen(
                onHandlePreflight = {
                    Log.d(
                        tag,
                        "Called 'onHandlePreflight' behaviour"
                    )
                },
                onPresentEngagement = {
                    controller.navigateToHolderPresentQrScreen()
                }
            )
        }
    }
}
