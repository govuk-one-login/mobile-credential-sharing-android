package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.dialogue.DialogueButtonParameters
import uk.gov.android.ui.componentsv2.dialogue.GdsDialogue
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R

/**
 * The "Are you sure you want to stop the check?" confirmation dialogue, wired up to the journey.
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
    val context = LocalContext.current
    val stopButtonRes = R.string.holder_stop_check_dialog_stop_button
    val continueButtonRes = R.string.holder_stop_check_dialog_continue_button

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderCancellationDialog")
    }

    HolderCancellationDialog(
        onStopTheCheck = { viewModel.stopTheCheck(context.getEnglishString(stopButtonRes)) },
        onContinueWithTheCheck = {
            viewModel.continueWithTheCheck(context.getEnglishString(continueButtonRes))
            onDismiss()
        },
        onDismissRequest = onDismiss,
        modifier = modifier
    )
}

@Composable
internal fun HolderCancellationDialog(
    onStopTheCheck: () -> Unit,
    onContinueWithTheCheck: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    GdsDialogue(
        headingText = stringResource(R.string.holder_stop_check_dialog_title),
        contentText = stringResource(R.string.holder_stop_check_dialog_body),
        buttonParameters = persistentListOf(
            DialogueButtonParameters(
                text = stringResource(R.string.holder_stop_check_dialog_stop_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onStopTheCheck
            ),
            DialogueButtonParameters(
                text = stringResource(R.string.holder_stop_check_dialog_continue_button),
                buttonType = ButtonTypeV2.Secondary(),
                onClick = onContinueWithTheCheck
            )
        ),
        modifier = modifier,
        onDismissRequest = onDismissRequest
    )
}

@PreviewContent
@Composable
internal fun HolderCancellationDialogPreview() {
    GdsTheme {
        Surface {
            HolderCancellationDialog(
                onStopTheCheck = {},
                onContinueWithTheCheck = {},
                onDismissRequest = {}
            )
        }
    }
}
