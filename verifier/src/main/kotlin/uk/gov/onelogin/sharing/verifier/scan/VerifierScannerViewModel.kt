package uk.gov.onelogin.sharing.verifier.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.verifier.scan.state.CompleteVerifierScannerState
import uk.gov.onelogin.sharing.verifier.scan.state.VerifierScannerState
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResult

class VerifierScannerViewModel @JvmOverloads constructor(
    state: VerifierScannerState.Complete = CompleteVerifierScannerState()
) : ViewModel(),
    VerifierScannerState.Complete by state {

    private val _isNavigationAllowed = MutableStateFlow(true)
    val isNavigationAllowed: StateFlow<Boolean> = _isNavigationAllowed

    val navigationTarget = combine(
        barcodeDataResult,
        isNavigationAllowed
    ) { result, shouldNavigate ->
        if (shouldNavigate) {
        } else {
        }
    }

    fun allowNavigation(): Job = updateNavigation(true)

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

    fun stopNavigation(): Job = updateNavigation(false)

    fun updateNavigation(isNavigationAllowed: Boolean): Job = viewModelScope.launch {
        _isNavigationAllowed.update { isNavigationAllowed }
    }
}
