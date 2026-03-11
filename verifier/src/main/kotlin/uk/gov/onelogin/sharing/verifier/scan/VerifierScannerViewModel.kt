package uk.gov.onelogin.sharing.verifier.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.verifier.VerifierNavigationEvents
import uk.gov.onelogin.sharing.verifier.scan.state.VerifierScannerState

@ContributesIntoMap(ViewModelScope::class, binding = binding<ViewModel>())
@Inject
@ViewModelKey(VerifierScannerViewModel::class)
class VerifierScannerViewModel(
    state: VerifierScannerState.Complete,
    private val orchestrator: Orchestrator.Verifier
) : ViewModel(),
    VerifierScannerState.Complete by state {

    private val _navigationEvents = MutableSharedFlow<VerifierNavigationEvents>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        viewModelScope.launch {

            barcodeDataResult.collectLatest {
                when (it) {
                    is BarcodeDataResult.Invalid -> {
                        processQrCode(it)
                        _navigationEvents.emit(
                            VerifierNavigationEvents.NavigateToInvalidScreen(
                                it.data
                            )
                        )
                    }

                    BarcodeDataResult.NotFound -> Unit

                    is BarcodeDataResult.Valid -> {
                        processQrCode(it)
                        _navigationEvents.emit(
                            VerifierNavigationEvents.NavigateToDiagnostic(
                                it.data
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        reset()
        super.onCleared()
    }

    private fun reset(): Job = viewModelScope.launch {
        resetBarcodeData()
    }

    private fun resetBarcodeData(): Job = viewModelScope.launch {
        update(result = BarcodeDataResult.NotFound)
    }

    private fun processQrCode(qrCode: BarcodeDataResult) {
        orchestrator.processQrCode(qrCode)
    }
}
