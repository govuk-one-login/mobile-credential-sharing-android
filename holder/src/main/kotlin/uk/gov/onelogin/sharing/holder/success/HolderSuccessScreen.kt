package uk.gov.onelogin.sharing.holder.success

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.holder.R

@Composable
internal fun HolderSuccessScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderSuccessViewModel = metroViewModel(),
) {
    val scope = rememberCoroutineScope()
    BackHandler(enabled = true) {
        scope.launch { viewModel.reset() }
    }

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderSuccessScreen")
    }

    HolderSuccessContent(modifier = modifier)
}

@Composable
internal fun HolderSuccessContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.holder_success_unfulfillable_request_title),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
@Preview(showBackground = true)
internal fun HolderSuccessScreenPreview() {
    HolderSuccessContent(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    )
}
