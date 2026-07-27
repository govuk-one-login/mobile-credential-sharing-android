package uk.gov.onelogin.sharing.verifier.error

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.orchestration.error.UnrecoverableErrorContent
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verifier.error.UnrecoverableVerifierViewModel.NavigationEvent as ViewModelEvent

@Composable
internal fun UnrecoverableVerifierErrorScreen(
    modifier: Modifier = Modifier,
    viewModel: UnrecoverableVerifierViewModel = metroViewModel(),
    onExitJourney: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val currentOnExitJourney by rememberUpdatedState(onExitJourney)
    val failureState: VerifierSessionState.Complete.Failed? by viewModel
        .failureState
        .collectAsStateWithLifecycle()

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("UnrecoverableVerifierErrorScreen")
    }

    BackHandler(true) {
        scope.launch {
            viewModel.exitJourney()
            currentOnExitJourney()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ViewModelEvent.ExitJourney -> {
                    currentOnExitJourney()
                }

                else -> {
                    // do nothing with null events
                }
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        failureState?.let {
            UnrecoverableErrorContent(
                it.error,
                modifier = Modifier.fillMaxSize(),
                onExitJourney = viewModel::exitJourney
            )
        } ?: CircularProgressIndicator()
    }
}
