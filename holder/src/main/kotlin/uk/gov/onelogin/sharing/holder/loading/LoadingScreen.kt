package uk.gov.onelogin.sharing.holder.loading

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

@Composable
internal fun LoadingScreen(
    type: LoadingScreenType,
    viewModel: LoadingViewModel = metroViewModel()
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(type) {
        metrics.putScreenState(type.name)
        viewModel.logScreenView(type)
    }

    LoadingContent(type = type)
}

@Composable
internal fun LoadingContent(type: LoadingScreenType) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(type.labelRes),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
@Preview(showBackground = true)
internal fun LoadingScreenGettingDetailsPreview() {
    LoadingContent(type = LoadingScreenType.GettingDetails)
}

@Composable
@Preview(showBackground = true)
internal fun LoadingScreenSharingDetailsPreview() {
    LoadingContent(type = LoadingScreenType.SharingDetails)
}
