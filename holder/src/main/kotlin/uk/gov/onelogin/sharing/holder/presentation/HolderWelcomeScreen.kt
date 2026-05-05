@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.Dispatchers
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.holder.QrCodeImage

private const val QR_SIZE = 800

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HolderWelcomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderWelcomeViewModel = metroViewModel(),
) {
    val contentState by viewModel.uiState.collectAsStateWithLifecycle(
        context = Dispatchers.Default
    )

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderWelcomeScreen")
    }

    QrContent(
        contentState = contentState,
        modifier = modifier
    )
}

@Composable
fun QrContent(contentState: HolderWelcomeUiState, modifier: Modifier = Modifier) {
    HolderWelcomeText()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        contentState.qrData?.let {
            QrCodeImage(
                data = it,
                size = QR_SIZE
            )
        }
    }
}

@Composable
@Preview
internal fun HolderWelcomeScreenPreview() {
    val contentState = HolderWelcomeUiState(qrData = "QR Data")

    QrContent(contentState, Modifier)
}
