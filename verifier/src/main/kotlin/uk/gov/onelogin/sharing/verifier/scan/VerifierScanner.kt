package uk.gov.onelogin.sharing.verifier.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.orchestration.scan.CredentialScanner

@Composable
fun VerifierScanner(viewModel: VerifierScannerViewModel = metroViewModel()) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("VerifierScanner")
    }

    CredentialScanner(orchestrator = viewModel.orchestrator)
}
