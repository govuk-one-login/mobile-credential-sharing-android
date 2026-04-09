package uk.gov.onelogin.sharing.holder.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderViewModel.NavigationEvent as ViewModelEvent
import uk.gov.onelogin.sharing.orchestration.error.UnrecoverableErrorContent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Composable
internal fun UnrecoverableHolderErrorScreen(
    modifier: Modifier = Modifier,
    viewModel: UnrecoverableHolderViewModel = metroViewModel(),
    onExitJourney: () -> Unit = {}
) {
    val currentOnExitJourney by rememberUpdatedState(onExitJourney)
    val failureState: HolderSessionState.Complete.Failed? by viewModel
        .failureState
        .collectAsStateWithLifecycle()

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
