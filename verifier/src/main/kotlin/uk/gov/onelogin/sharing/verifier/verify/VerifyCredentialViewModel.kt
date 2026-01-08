package uk.gov.onelogin.sharing.verifier.verify

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.core.logger.logTag

@Inject
@ViewModelKey(VerifyCredentialViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class VerifyCredentialViewModel(
    private val logger: Logger,
    private val bluetoothStateMonitor: BluetoothStateMonitor
) : ViewModel() {
    private val initialState = VerifyCredentialUiState()
    private var allGranted: Boolean? = null
    private var bluetoothStatus: BluetoothStatus? = null

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<VerifyCredentialUiState> = _uiState

    init {
        bluetoothStateMonitor.start()
        viewModelScope.launch {
            bluetoothStateMonitor.states
                .distinctUntilChanged()
                .collect { status ->
                    bluetoothStatus = status
                    checkPreconditions()
                    logger.debug(logTag, "Bluetooth status: $status")
                }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        bluetoothStateMonitor.stop()

        super.onCleared()
    }

    fun onPermissionRequestLaunched() {
        _uiState.update {
            it.copy(
                hasPreviouslyRequestedPermission = true
            )
        }
    }

    fun onPermissionsChanged(allGranted: Boolean) {
        logger.debug(logTag, "Permissions changed. allGranted=$allGranted")
        this.allGranted = allGranted
        checkPreconditions()
    }

    private fun checkPreconditions() {
        val granted = allGranted
        val bluetooth = bluetoothStatus

        val newState = when {
            granted == null ->
                VerifyCredentialPreconditionsState.Idle

            !granted ->
                VerifyCredentialPreconditionsState.BluetoothAccessDenied

            bluetooth == null ->
                VerifyCredentialPreconditionsState.Idle

            bluetooth == BluetoothStatus.ON ->
                VerifyCredentialPreconditionsState.Met

            else ->
                VerifyCredentialPreconditionsState.BluetoothDisabled
        }

        setPreconditions(newState)
    }

    private fun setPreconditions(new: VerifyCredentialPreconditionsState) {
        if (_uiState.value.preconditionsState == new) return
        _uiState.update { it.copy(preconditionsState = new) }
    }
}

data class VerifyCredentialUiState(
    val hasPreviouslyRequestedPermission: Boolean = false,
    val preconditionsState: VerifyCredentialPreconditionsState =
        VerifyCredentialPreconditionsState.Idle
)
