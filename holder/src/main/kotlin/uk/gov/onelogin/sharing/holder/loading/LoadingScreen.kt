package uk.gov.onelogin.sharing.holder.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent

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
internal fun LoadingContent(
    type: LoadingScreenType,
    /**
     * Fixed progress used only to render a static, non-animating indicator in previews and
     * screenshot tests (an indeterminate [CircularProgressIndicator] animates forever, which stalls
     * Roborazzi's frame capture). Leave `null` for the real screen to keep the indeterminate spinner.
     */
    progress: Float? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (progress == null) {
            CircularProgressIndicator()
        } else {
            CircularProgressIndicator(progress = { progress })
        }
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
@PreviewContent
internal fun LoadingScreenGettingDetailsPreview() {
    GdsTheme {
        Surface {
            LoadingContent(type = LoadingScreenType.GettingDetails, progress = PREVIEW_PROGRESS)
        }
    }
}

@Composable
@PreviewContent
internal fun LoadingScreenSharingDetailsPreview() {
    GdsTheme {
        Surface {
            LoadingContent(type = LoadingScreenType.SharingDetails, progress = PREVIEW_PROGRESS)
        }
    }
}

/** Arbitrary fixed progress for a stable, non-animating preview/screenshot frame. */
private const val PREVIEW_PROGRESS = 0.7f
