package uk.gov.onelogin.sharing.holder.qrcode

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.Dispatchers
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.qrcode.component.LandscapeQrContent
import uk.gov.onelogin.sharing.holder.qrcode.component.PortraitQrContent

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
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    if (windowSizeClass.usesTwoColumnLayout()) {
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

/**
 * Two columns when the window is wide but short, e.g. a landscape phone.
 */
internal fun WindowSizeClass.usesTwoColumnLayout(): Boolean =
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
        !isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

internal class QrCodeScreenPreviewProvider : PreviewParameterProvider<QrCodeUiState> {
    override val values: Sequence<QrCodeUiState>
        get() = sequenceOf(
            QrCodeUiState("QR Data"),
            QrCodeUiState(null)
        )
}
