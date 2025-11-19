package uk.gov.onelogin.sharing.verifier.scan

import android.net.Uri
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

    private val _uri = MutableStateFlow<BarcodeDataResult>(BarcodeDataResult.NotFound)
    val uri: StateFlow<BarcodeDataResult> = _uri

    override fun onCleared() {
        reset()
        super.onCleared()
    }

    fun reset(): Job = viewModelScope.launch {
        _hasPreviouslyDeniedPermission.update { false }
        resetUri()
    }

    fun resetUri(): Job = viewModelScope.launch {
        _uri.update { BarcodeDataResult.NotFound }
    }

    fun update(hasPreviouslyDeniedPermission: Boolean): Job = viewModelScope.launch {
        _hasPreviouslyDeniedPermission.update { hasPreviouslyDeniedPermission }
    }

    fun update(uri: Uri): Job = viewModelScope.launch {
        _uri.update { BarcodeDataResult.Found(uri) }
    }
}
