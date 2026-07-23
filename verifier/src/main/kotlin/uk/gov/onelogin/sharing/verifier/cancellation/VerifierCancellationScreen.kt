package uk.gov.onelogin.sharing.verifier.cancellation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder

@Composable
internal fun VerifierCancellationScreen(
    modifier: Modifier = Modifier,
    viewModel: VerifierCancellationScreenViewModel = metroViewModel(),
    onCancelJourney: () -> Unit = {}
) = VerifierCancellationScreen(
    modifier = modifier,
    onCancel = {
        viewModel.reset()
        onCancelJourney()
    }
)

@Composable
internal fun VerifierCancellationScreen(modifier: Modifier = Modifier, onCancel: () -> Unit = {}) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val currentOnCancel by rememberUpdatedState(onCancel)

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("VerifierCancellationScreen")
    }

    LaunchedEffect(Unit) {
        scope.launch { currentOnCancel() }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag("progressIndicator")
        )
    }
}

@Preview
@Composable
internal fun VerifierCancellationScreenPreview() {
    GdsTheme {
        VerifierCancellationScreen(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(spacingDouble)
        )
    }
}
