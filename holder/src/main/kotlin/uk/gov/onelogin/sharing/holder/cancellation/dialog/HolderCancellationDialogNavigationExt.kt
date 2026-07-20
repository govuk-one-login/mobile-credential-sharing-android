package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.bannerElevation
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingSingle

data object HolderCancellationDialogNavigationExt {
    fun NavController.navigateToHolderUserCancellationDialog(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderCancellationDialogRoute, options)

    internal fun NavGraphBuilder.configureHolderUserCancellationDialog() {
        dialog<HolderCancellationDialogRoute> {
            val scope = rememberCoroutineScope()
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
            HolderCancellationDialogContents(
                modifier = Modifier
                    .dropShadow(
                        shape = RoundedCornerShape(spacingDouble),
                        shadow = Shadow(
                            radius = spacingSingle,
                            spread = spacingSingle,
                            color = MaterialTheme.colorScheme.scrim,
                            offset = DpOffset(x = bannerElevation, y = bannerElevation)
                        )
                    )
                    .clip(RoundedCornerShape(spacingDouble))
                    .background(GdsLocalColorScheme.current.dialogBackground)
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
