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
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<VerifyCredentialUiState> = _uiState

    init {
        bluetoothStateMonitor.start()
        viewModelScope.launch {
            bluetoothStateMonitor.states.collect {
                updateBluetoothState(it)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        super.onCleared()

        bluetoothStateMonitor.stop()
    }

    fun updateBluetoothState(status: BluetoothStatus) {
        when (status) {
            BluetoothStatus.ON -> {
                logger.debug(
                    logTag,
                    "User enabled bluetooth via prompt"
                )

                _uiState.update {
                    it.copy(
                        preconditionsState = VerifyCredentialPreconditionsState.Met
                    )
                }
            }

            else -> {
                logger.debug(
                    logTag,
                    "User cancelled bluetooth prompt"
                )

                _uiState.update {
                    it.copy(
                        preconditionsState = VerifyCredentialPreconditionsState.BluetoothDisabled
                    )
                }
            }
        }
    }
}

data class VerifyCredentialUiState(
    val preconditionsState: VerifyCredentialPreconditionsState =
        VerifyCredentialPreconditionsState.BluetoothDisabled
)
