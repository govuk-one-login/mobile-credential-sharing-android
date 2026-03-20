package uk.gov.onelogin.sharing.verifier.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.verifier.VerifierNavigationEvents
import uk.gov.onelogin.sharing.verifier.scan.state.VerifierUiState

@Composable
fun VerifierScanner(
    viewModel: VerifierScannerViewModel = metroViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onInvalidBarcode: (String) -> Unit = {},
    onValidBarcode: (String) -> Unit = {},
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

    if (uiState is VerifierUiState.StartScanner) {
        content()
    }
}
