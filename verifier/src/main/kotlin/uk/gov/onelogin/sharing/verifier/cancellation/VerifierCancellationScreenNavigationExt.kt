package uk.gov.onelogin.sharing.verifier.cancellation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.android.ui.theme.spacingDouble

data object VerifierCancellationScreenNavigationExt {
    fun NavController.navigateToVerifierUserCancellationScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(VerifierCancellationScreenRoute, options)

    internal fun NavGraphBuilder.configureVerifierUserCancellationScreen() {
        composable<VerifierCancellationScreenRoute> {
            val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
            VerifierCancellationScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacingDouble),
                onCancelJourney = {
                    backPressDispatcher
                        ?.onBackPressedDispatcher
                        ?.onBackPressed()
                }
            )
        }
    }
}
