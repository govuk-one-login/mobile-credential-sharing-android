package uk.gov.onelogin.sharing.holder.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
fun QrCodeScreen(modifier: Modifier = Modifier, viewModel: QrCodeViewModel = metroViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        context = Dispatchers.Default
    )
    val context = LocalContext.current

    val metrics = rememberMetricsStateHolder()
    val titleRes = R.string.holder_qr_code_heading
    LaunchedEffect(Unit) {
        metrics.putScreenState("QrCodeScreen")
        viewModel.sendScreenEvent(context.getEnglishString(titleRes))
    }

    QrContent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
private fun QrContent(uiState: QrCodeUiState, modifier: Modifier = Modifier) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeQrContent(uiState.qrPayload, modifier)
    } else {
        PortraitQrContent(uiState.qrPayload, modifier)
    }
}

@Composable
@PreviewContent
internal fun QrCodeScreenPreview(
    @PreviewParameter(QrCodeScreenPreviewProvider::class) uiState: QrCodeUiState
) {
    QrContent(
        uiState = uiState,
        modifier = Modifier
    )
}

internal class QrCodeScreenPreviewProvider : PreviewParameterProvider<QrCodeUiState> {
    override val values: Sequence<QrCodeUiState>
        get() = sequenceOf(
            QrCodeUiState("QR Data"),
            QrCodeUiState(null)
        )
}
