package uk.gov.onelogin.sharing.cameraService.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.state.ScannerState

@ContributesIntoMap(ViewModelScope::class, binding = binding<ViewModel>())
@Inject
@ViewModelKey(ScannerViewModel::class)
class ScannerViewModel(
    state: ScannerState.Complete,
    private val observer: ScanObserver,
    private val orchestratorInteractor: OrchestratorInteractor
) : ViewModel(),
    ScannerState.Complete by state {

    init {
        viewModelScope.launch {

            barcodeDataResult.collectLatest {
                when (it) {
                    is BarcodeDataResult.Invalid -> {
                        println("Invalid QR CODE")
                        observer.onScanResult(it)
                    }

                    BarcodeDataResult.NotFound -> Unit

                    is BarcodeDataResult.Valid -> {
                        println("Valid QR CODE")
                        observer.onScanResult(it)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        if (barcodeDataResult.value is BarcodeDataResult.NotFound) {
            orchestratorInteractor.cancel()
        }
        reset()
        super.onCleared()
    }

    private fun reset(): Job = viewModelScope.launch {
        resetBarcodeData()
    }

    private fun resetBarcodeData(): Job = viewModelScope.launch {
        update(result = BarcodeDataResult.NotFound)
    }


}
