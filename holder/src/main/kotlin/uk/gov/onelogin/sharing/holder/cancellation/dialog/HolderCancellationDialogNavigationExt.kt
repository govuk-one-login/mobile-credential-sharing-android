package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.spacingDouble

data object HolderCancellationDialogNavigationExt {
    fun NavController.navigateToHolderUserCancellationDialog(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderCancellationDialogRoute, options)

    internal fun NavGraphBuilder.configureHolderUserCancellationDialog() {
        dialog<HolderCancellationDialogRoute> {
            val scope = rememberCoroutineScope()
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current

            Surface(
                shape = RoundedCornerShape(spacingDouble),
                border = BorderStroke(1.dp, Color.Gray),
            ) {
                HolderCancellationDialogContents(
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
}
