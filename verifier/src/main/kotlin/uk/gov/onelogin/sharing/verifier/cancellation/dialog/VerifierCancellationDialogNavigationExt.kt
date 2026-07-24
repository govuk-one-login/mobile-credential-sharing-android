package uk.gov.onelogin.sharing.verifier.cancellation.dialog

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.spacingDouble

data object VerifierCancellationDialogNavigationExt {
    fun NavController.navigateToVerifierUserCancellationDialog(
        options: NavOptionsBuilder.() -> Unit = {},
    ) = navigate(VerifierCancellationDialogRoute, options)

    internal fun NavGraphBuilder.configureVerifierUserCancellationDialog() {
        dialog<VerifierCancellationDialogRoute> {
            val scope = rememberCoroutineScope()
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current

            VerifierCancellationDialogContents(
                modifier = Modifier
                    .padding(spacingDouble),
                onDismiss = {
                    scope.launch {
                        backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            )
        }
    }
}
