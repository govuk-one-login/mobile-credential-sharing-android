package uk.gov.onelogin.sharing.holder.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.Dispatchers
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.presentation.component.LandscapeQrContent
import uk.gov.onelogin.sharing.holder.presentation.component.PortraitQrContent
import uk.gov.onelogin.sharing.orchestration.holder.session.EngagementPresentation

@Composable
fun HolderWelcomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderWelcomeViewModel = metroViewModel()
) {
    val contentState by viewModel.uiState.collectAsStateWithLifecycle(
        context = Dispatchers.Default
    )
    val context = LocalContext.current

    val metrics = rememberMetricsStateHolder()
    val titleRes = R.string.holder_welcome_heading
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderWelcomeScreen")
        viewModel.sendScreenEvent(context.getEnglishString(titleRes))
    }

    QrContent(
        presentation = contentState,
        modifier = modifier
    )
}

@Composable
private fun QrContent(presentation: EngagementPresentation, modifier: Modifier = Modifier) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeQrContent(presentation.qrPayload, modifier)
    } else {
        PortraitQrContent(presentation.qrPayload, modifier)
    }
}

@Composable
@PreviewContent
internal fun HolderWelcomeScreenPreview() {
    QrContent(
        presentation = EngagementPresentation(
            qrPayload = "QR Data",
            cancelRequiresConfirmation = false
        ),
        modifier = Modifier
    )
}
