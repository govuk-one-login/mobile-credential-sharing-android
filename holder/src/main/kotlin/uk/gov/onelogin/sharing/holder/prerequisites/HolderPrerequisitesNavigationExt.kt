package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.android.ui.theme.spacingDouble

object HolderPrerequisitesNavigationExt {
    fun NavController.navigateToHolderPrerequisitesScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderPrerequisitesRoute, options)

    internal fun NavGraphBuilder.configureHolderPrerequisitesScreen() {
        composable<HolderPrerequisitesRoute> {
            HolderPrerequisitesScreen(
                modifier = Modifier.fillMaxSize()
                    .padding(spacingDouble)
            )
        }
    }
}
