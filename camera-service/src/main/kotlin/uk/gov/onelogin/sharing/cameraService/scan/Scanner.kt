package uk.gov.onelogin.sharing.cameraService.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult

@Composable
fun Scanner(
    modifier: Modifier = Modifier,
    viewModel: ScannerViewModel = metroViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onInvalidBarcode: (String) -> Unit = {},
    onValidBarcode: (String) -> Unit = {}
) {
    val currentOnInvalidBarcode by rememberUpdatedState(onInvalidBarcode)
    val currentOnValidBarcode by rememberUpdatedState(onValidBarcode)

    val barcodeScanResultCallback: BarcodeScanResult.Callback = QrScannerCallback(
        onQrDetected = viewModel::update
    )

    VerifierScannerContent(
        lifecycleOwner = lifecycleOwner,
        modifier = modifier,
        barcodeScanResultCallback = barcodeScanResultCallback
    )
}
