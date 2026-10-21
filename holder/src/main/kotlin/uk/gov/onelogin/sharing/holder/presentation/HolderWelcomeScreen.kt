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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.Dispatchers
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
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

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderWelcomeScreen")
        viewModel.sendScreenEvent()
    }

    QrContent(
        contentState = contentState,
        modifier = modifier
    )
}

@Composable
private fun QrContent(contentState: HolderWelcomeUiState, modifier: Modifier = Modifier) {
    val qrData = contentState.qrData

    if (qrData == null) {
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
@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 858
)
private fun HolderWelcomeScreenPortraitPreview() {
    QrContent(HolderWelcomeUiState(qrData = "QR Data"))
}

@Composable
@Preview(
    showBackground = true,
    widthDp = 858,
    heightDp = 360
)
private fun HolderWelcomeScreenLandscapePreview() {
    QrContent(HolderWelcomeUiState(qrData = "QR Data"))
}
