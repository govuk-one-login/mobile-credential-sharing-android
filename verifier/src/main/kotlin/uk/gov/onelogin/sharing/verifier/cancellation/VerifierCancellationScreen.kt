package uk.gov.onelogin.sharing.verifier.cancellation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.orchestration.cancellation.CancellationScreen

@Composable
internal fun VerifierCancellationScreen(
    modifier: Modifier = Modifier,
    viewModel: VerifierCancellationScreenViewModel = metroViewModel(),
    onCancelJourney: () -> Unit = {}
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("VerifierCancellationScreen")
    }

    CancellationScreen(
        modifier = modifier,
        onCancel = {
            viewModel.reset()
            onCancelJourney()
        }
    )
}
