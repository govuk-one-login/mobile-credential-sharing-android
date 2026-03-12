package uk.gov.onelogin.sharing.verifier.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.verifier.scan.state.VerifierScannerState
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResult

@ContributesIntoMap(VerifierUiScope::class, binding = binding<ViewModel>())
@Inject
@ViewModelKey(VerifierScannerViewModel::class)
class VerifierScannerViewModel(state: VerifierScannerState.Complete) :
    ViewModel(),
    VerifierScannerState.Complete by state {

    override fun onCleared() {
        reset()
        super.onCleared()
    }

    fun reset(): Job = viewModelScope.launch {
        update(hasPreviouslyDeniedPermission = false)
        resetBarcodeData()
    }

    fun resetBarcodeData(): Job = viewModelScope.launch {
        update(result = BarcodeDataResult.NotFound)
    }
}
