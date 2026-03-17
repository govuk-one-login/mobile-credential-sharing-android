package uk.gov.onelogin.sharing.verifier.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.cameraService.scan.QrScannerCallback
import uk.gov.onelogin.sharing.verifier.VerifierNavigationEvents

@Composable
fun VerifierScanner(
    modifier: Modifier = Modifier,
    viewModel: VerifierScannerViewModel = metroViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onInvalidBarcode: (String) -> Unit = {},
    onValidBarcode: (String) -> Unit = {}
) {
    val currentOnInvalidBarcode by rememberUpdatedState(onInvalidBarcode)
    val currentOnValidBarcode by rememberUpdatedState(onValidBarcode)

    LaunchedEffect(viewModel.navigationEvents) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvents.collect { event ->
                when (event) {
                    is VerifierNavigationEvents.NavigateToDiagnostic -> currentOnValidBarcode(
                        event.qrCode
                    )

                    is VerifierNavigationEvents.NavigateToInvalidScreen -> currentOnInvalidBarcode(
                        event.qrCode
                    )
                }
            }
        }
    }

    val barcodeScanResultCallback: BarcodeScanResult.Callback = QrScannerCallback(
        onQrDetected = viewModel::update
    )

    VerifierScannerContent(
        lifecycleOwner = lifecycleOwner,
        modifier = modifier,
        barcodeScanResultCallback = barcodeScanResultCallback
    )
}
