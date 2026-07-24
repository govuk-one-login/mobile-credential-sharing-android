package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.orchestration.cancellation.CancellationDialogContents

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

    CancellationDialogContents(
        modifier = modifier,
        onCancel = viewModel::cancelJourney,
        onDismiss = onDismiss
    )
}
