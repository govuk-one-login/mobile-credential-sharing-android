package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.holder.R
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
    val progressTextResource: String? = calculateProgressTextFrom(state)?.let {
        stringResource(it)
    }

    HolderPrerequisitesContent(
        modifier = modifier,
        progressText = progressTextResource
    )

    LaunchedEffect(state) {
        when (state) {
            is HolderSessionState.Preflight -> {
                currentOnHandlePreflight()
            }

            is HolderSessionState.PresentingEngagement -> {
                currentOnPresentEngagement()
            }

            is HolderSessionState.Complete.Failed -> {
                currentOnUnrecoverableError()
            }

            else -> {
                // other session states don't affect this screen's behaviour
            }
        }
    }
}

@Composable
internal fun HolderPrerequisitesContent(
    modifier: Modifier = Modifier,
    progressText: String? = null
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
            progressText?.let { Text(it) }
        }
    }
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
            progressText = calculateProgressTextFrom(state)?.let { stringResource(it) }
        )
    }
}
