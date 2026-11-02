package uk.gov.onelogin.sharing.holder.loading

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicator
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicatorDefaults
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent

@Composable
internal fun LoadingScreen(
    type: LoadingScreenType,
    viewModel: LoadingViewModel = metroViewModel()
) {
    BackHandler(enabled = type.blocksBackNavigation) { }

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(type) {
        metrics.putScreenState(type.name)
        viewModel.logScreenView(type)
    }

    LoadingContent(type = type)
}

@Composable
internal fun LoadingContent(type: LoadingScreenType) {
    val label = stringResource(type.labelRes)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GdsProgressIndicator(
            labels = GdsProgressIndicatorDefaults.labels(
                short = label,
                long = label,
                longer = label
            )
        )
    }
}

@Composable
@PreviewContent
internal fun LoadingScreenGettingDetailsPreview() {
    GdsTheme {
        Surface {
            LoadingContent(type = LoadingScreenType.GettingDetails)
        }
    }
}

@Composable
@PreviewContent
internal fun LoadingScreenSharingDetailsPreview() {
    GdsTheme {
        Surface {
            LoadingContent(type = LoadingScreenType.SharingDetails)
        }
    }
}
