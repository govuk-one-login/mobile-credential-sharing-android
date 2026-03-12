package uk.gov.onelogin.sharing.verifier.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.cameraService.scan.Scanner

@Composable
fun VerifierScanner(
    modifier: Modifier = Modifier,
    viewModel: VerifierScannerViewModel = metroViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onInvalidBarcode: (String) -> Unit = {},
    onValidBarcode: (String) -> Unit = {}
) {
    Scanner()
}
