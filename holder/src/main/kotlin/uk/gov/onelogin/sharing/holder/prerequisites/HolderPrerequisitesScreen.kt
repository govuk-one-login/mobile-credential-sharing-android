package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesViewModel.NavigationEvent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Composable
internal fun HolderPrerequisitesScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderPrerequisitesViewModel = metroViewModel(),
    onHandlePreflight: () -> Unit = {},
    onPresentEngagement: () -> Unit = {},
    onUnrecoverableError: () -> Unit = {}
) {
    val currentOnHandlePreflight by rememberUpdatedState(onHandlePreflight)
    val currentOnPresentEngagement by rememberUpdatedState(onPresentEngagement)
    val currentOnUnrecoverableError by rememberUpdatedState(onUnrecoverableError)
    val state: HolderSessionState by viewModel.holderSessionState.collectAsStateWithLifecycle()
    val progressTextResource: Int? by loadProgressText(state)
    val progressText: String? = progressTextResource?.let {
        stringResource(it)
    }

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderPrerequisitesScreen")
    }

    HolderPrerequisitesContent(
        modifier = modifier
    ) { progressText }

    LaunchedEffect(state) {
        viewModel.events.collect { event ->
            when (event) {
                is NavigationEvent.ToPreflight -> currentOnHandlePreflight()
                is NavigationEvent.PresentEngagement -> currentOnPresentEngagement()
                is NavigationEvent.ToUnrecoverableError -> currentOnUnrecoverableError()
            }
        }
    }
}

@Composable
internal fun HolderPrerequisitesContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> String? = { null }
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacingSingle)
        ) {
            CircularProgressIndicator()
            content()?.let { Text(it) }
        }
    }
}

@Composable
fun loadProgressText(
    state: HolderSessionState,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) = produceState<Int?>(null, state) {
    value = withContext(dispatcher) { calculateProgressTextFrom(state) }
}

private fun calculateProgressTextFrom(state: HolderSessionState): Int? = when (state) {
    HolderSessionState.NotStarted ->
        R.string.holder_prerequisites_not_started

    is HolderSessionState.Preflight ->
        R.string.holder_prerequisites_preflight

    is HolderSessionState.ReadyToPresent ->
        R.string.holder_prerequisites_ready_to_present

    is HolderSessionState.PresentingEngagement ->
        R.string.holder_prerequisites_presenting_engagement

    else -> null
}

@Composable
@Preview(showBackground = true)
internal fun HolderPrerequisitesScreenPreview(
    @PreviewParameter(HolderPrerequisitesStates::class)
    state: HolderSessionState
) {
    GdsTheme {
        HolderPrerequisitesContent(
            modifier = Modifier.fillMaxSize(),
            content = { calculateProgressTextFrom(state)?.let { stringResource(it) } }
        )
    }
}
