package uk.gov.onelogin.sharing.sdk.api.verifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import uk.gov.onelogin.sharing.cameraService.scan.Scanner

/**
 * Composable that provides a QR code scanner for the Verifier role.
 *
 * This handles all internal dependency wiring. Consumers only need a [CredentialVerifier]
 * instance obtained from the SDK.
 *
 * @param credentialVerifier The verifier instance from the SDK.
 * @param modifier Optional [Modifier] to apply to the scanner.
 */
@Composable
fun VerifierScanner(credentialVerifier: CredentialVerifier, modifier: Modifier = Modifier) {
    val factory = remember(credentialVerifier.orchestrator) {
        createGraphFactory<ScannerGraph.Factory>()
            .create(credentialVerifier.orchestrator)
            .metroViewModelFactory
    }

    CompositionLocalProvider(LocalMetroViewModelFactory provides factory) {
        Scanner(modifier = modifier)
    }
}
