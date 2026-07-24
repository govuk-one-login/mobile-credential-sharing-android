package uk.gov.onelogin.sharing.holder.cancellation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.orchestration.cancellation.CancellationScreen

@Composable
internal fun HolderCancellationScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderCancellationScreenViewModel = metroViewModel(),
    onCancelJourney: () -> Unit = {}
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderCancellationScreen")
    }

    CancellationScreen(
        modifier = modifier,
        onCancel = {
            viewModel.reset()
            onCancelJourney()
        }
    )
}
