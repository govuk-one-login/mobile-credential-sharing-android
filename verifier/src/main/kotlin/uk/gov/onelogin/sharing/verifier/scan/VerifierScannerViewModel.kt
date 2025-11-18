package uk.gov.onelogin.sharing.verifier.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerifierScannerViewModel : ViewModel() {
    private val _hasPreviouslyDeniedPermission = MutableStateFlow(false)
    val hasPreviouslyDeniedPermission: StateFlow<Boolean> = _hasPreviouslyDeniedPermission

    override fun onCleared() {
        reset()
        super.onCleared()
    }

    fun reset(): Job = viewModelScope.launch {
        _hasPreviouslyDeniedPermission.update { false }
    }

    fun update(hasPreviouslyDeniedPermission: Boolean): Job = viewModelScope.launch {
        _hasPreviouslyDeniedPermission.update { hasPreviouslyDeniedPermission }
    }
}
