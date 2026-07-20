package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder

@Composable
internal fun HolderCancellationScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderCancellationDialogViewModel = metroViewModel(),
    onDismiss: () -> Unit = {},
) = HolderCancellationScreen(
    modifier = modifier,
    onCancelJourney = viewModel::cancelJourney,
    onDismiss = onDismiss,
)

@Composable
internal fun HolderCancellationScreen(
    onCancelJourney: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderCancellationScreen")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacingDouble),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Are you sure you want to cancel?")

        Button(
            onClick = onCancelJourney,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Yes")
        }
        Button(
            onClick = onDismiss,
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("No")
        }
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