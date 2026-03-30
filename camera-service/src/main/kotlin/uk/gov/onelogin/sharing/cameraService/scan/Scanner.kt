package uk.gov.onelogin.sharing.cameraService.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult

@Composable
fun Scanner(
    onScanResult: (String?) -> Unit,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val barcodeScanResultCallback: BarcodeScanResult.Callback = QrScannerCallback(
        onQrDetected = {
            onScanResult(it)
        }
    )

    ScannerContent(
        lifecycleOwner = lifecycleOwner,
        modifier = modifier,
        barcodeScanResultCallback = barcodeScanResultCallback
    )
}
