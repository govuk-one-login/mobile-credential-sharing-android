package uk.gov.onelogin.sharing.holder.sharingdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.holder.R

@Composable
internal fun SharingDetailsScreen(viewModel: SharingDetailsViewModel = metroViewModel()) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("SharingDetailsScreen")
        viewModel.logScreenView()
    }

    SharingDetailsContent()
}

@Composable
internal fun SharingDetailsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.holder_sharing_details_label),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
@Preview(showBackground = true)
internal fun SharingDetailsScreenPreview() {
    SharingDetailsContent()
}
