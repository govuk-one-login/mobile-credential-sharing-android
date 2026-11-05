package uk.gov.onelogin.sharing.holder.loading

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicator
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicatorDefaults
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicatorState
import uk.gov.android.ui.componentsv2.progress.ProgressWaitLength
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreen
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent

@Composable
internal fun LoadingScreen(
    type: LoadingScreenType,
    viewModel: LoadingViewModel = metroViewModel()
) {
    val context = LocalContext.current
    val titleRes = type.labelRes
    BackHandler(enabled = type.blocksBackNavigation) { }

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(type) {
        metrics.putScreenState(type.name)
        viewModel.logScreenView(type, context.getEnglishString(titleRes))
    }

    LoadingContent(type = type)
}

@Composable
internal fun LoadingContent(
    type: LoadingScreenType,
    state: GdsProgressIndicatorState = GdsProgressIndicatorState()
) {
    val label = stringResource(type.labelRes)
    CentreAlignedScreen(
        modifier = Modifier.fillMaxSize(),
        title = {},
        image = {
            GdsProgressIndicator(
                labels = GdsProgressIndicatorDefaults.labels(
                    short = label,
                    long = label,
                    longer = label
                ),
                state = state
            )
        }
    )
}

@Composable
@PreviewContent
internal fun LoadingScreenGettingDetailsPreview() {
    GdsTheme {
        Surface {
            LoadingContent(
                type = LoadingScreenType.GettingDetails,
                state = previewProgressState()
            )
        }
    }
}

@Composable
@PreviewContent
internal fun LoadingScreenSharingDetailsPreview() {
    GdsTheme {
        Surface {
            LoadingContent(
                type = LoadingScreenType.SharingDetails,
                state = previewProgressState()
            )
        }
    }
}

private fun previewProgressState() =
    GdsProgressIndicatorState(initialWaitedFor = ProgressWaitLength.Short)
