package uk.gov.onelogin.sharing.holder.cancellation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.android.ui.theme.spacingDouble

data object HolderCancellationScreenNavigationExt {
    fun NavController.navigateToHolderUserCancellationScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderCancellationScreenRoute, options)

    internal fun NavGraphBuilder.configureHolderUserCancellationScreen() {
        composable<HolderCancellationScreenRoute> {
            val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
            HolderCancellationScreen(
                modifier = Modifier.fillMaxSize()
                    .padding(spacingDouble),
                onCancelJourney = {
                    backPressDispatcher?.onBackPressedDispatcher?.onBackPressed()
                }
            )
        }
    }
}
