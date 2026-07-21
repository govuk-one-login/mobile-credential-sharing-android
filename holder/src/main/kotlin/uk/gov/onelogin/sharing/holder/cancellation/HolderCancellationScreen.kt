// Suppresses "compose:lambda-param-in-effect" due to linting being unaware of the provided scope within the
// `LaunchedEffect`.
@file:Suppress("ktlint:compose:lambda-param-in-effect")

package uk.gov.onelogin.sharing.holder.cancellation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
internal fun HolderCancellationScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderCancellationScreenViewModel = metroViewModel(),
    onCancelJourney: () -> Unit = {}
) = HolderCancellationScreen(
    modifier = modifier,
    onCancel = {
        viewModel.reset()
        onCancelJourney()
    }
)

@Composable
internal fun HolderCancellationScreen(modifier: Modifier = Modifier, onCancel: () -> Unit = {}) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderCancellationScreen")
    }

    LaunchedEffect(Unit) {
        scope.launch { onCancel() }
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
internal fun HolderCancellationScreenPreview() {
    GdsTheme {
        HolderCancellationScreen(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(spacingDouble)
        )
    }
}
