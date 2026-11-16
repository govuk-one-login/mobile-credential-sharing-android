package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.dialogue.DialogueButtonParameters
import uk.gov.android.ui.componentsv2.dialogue.GdsDialogue
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.holder.R

/**
 * The "Are you sure you want to stop the check?" confirmation dialogue.
 *
 * @param viewModel Handles the button behaviour and analytics.
 * @param onDismiss Called when the dialogue should close without stopping the check; for example
 * when the User chooses to continue with the check, or taps outside / goes back.
 */
@Composable
internal fun HolderCancellationDialogContents(
    modifier: Modifier = Modifier,
    viewModel: HolderCancellationDialogViewModel = metroViewModel(),
    onDismiss: () -> Unit = {}
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderCancellationDialog")
    }

    GdsDialogue(
        headingText = stringResource(R.string.holder_stop_check_dialog_title),
        contentText = stringResource(R.string.holder_stop_check_dialog_body),
        buttonParameters = persistentListOf(
            DialogueButtonParameters(
                text = stringResource(R.string.holder_stop_check_dialog_stop_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = { viewModel.stopTheCheck() }
            ),
            DialogueButtonParameters(
                text = stringResource(R.string.holder_stop_check_dialog_continue_button),
                buttonType = ButtonTypeV2.Secondary(),
                onClick = {
                    viewModel.continueWithTheCheck()
                    onDismiss()
                }
            )
        ),
        modifier = modifier,
        onDismissRequest = onDismiss
    )
}
