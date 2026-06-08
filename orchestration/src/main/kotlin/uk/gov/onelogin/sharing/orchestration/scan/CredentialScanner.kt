package uk.gov.onelogin.sharing.orchestration.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.cameraService.scan.Scanner
import uk.gov.onelogin.sharing.orchestration.Orchestrator

/**
 * Composable that provides a standalone QR code scanner for the Verifier role.
 * Use this if you're not using the pre-built verifier UI module.
 *
 * @param orchestrator The verifier orchestrator instance.
 * @param modifier Optional [Modifier] to apply to the scanner.
 */
@Composable
fun CredentialScanner(orchestrator: Orchestrator.Verifier, modifier: Modifier = Modifier) {
    val factory = remember(orchestrator) {
        createGraphFactory<ScannerGraph.Factory>()
            .create()
            .metroViewModelFactory
    }
    val scope = rememberCoroutineScope { Dispatchers.IO }

    CompositionLocalProvider(LocalMetroViewModelFactory provides factory) {
        Scanner(
            modifier = modifier,
            onScanResult = {
                scope.launch {
                    orchestrator.processQrCode(it)
                }
            }
        )
    }
}
