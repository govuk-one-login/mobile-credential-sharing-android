package uk.gov.onelogin.sharing.holder.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
        contentState = contentState,
        modifier = modifier
    )
}

@Composable
private fun QrContent(contentState: HolderWelcomeUiState, modifier: Modifier = Modifier) {
    val qrData = contentState.qrData

    if (qrData == null || !contentState.showQr) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeQrContent(qrData, modifier)
    } else {
        PortraitQrContent(qrData, modifier)
    }
}

@Composable
@PreviewContent
internal fun HolderWelcomeScreenPreview() {
    val contentState = HolderWelcomeUiState(qrData = "QR Data", showQr = true)
    QrContent(contentState, Modifier)
}
