@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.verifier.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.core.logger.logTag

@Inject
@ViewModelKey(SessionEstablishmentViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class SessionEstablishmentViewModel(
    private val bluetoothAdapterProvider: BluetoothAdapterProvider,
    private val scanner: BluetoothScanner,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectWithHolderDeviceState())
    val uiState: StateFlow<ConnectWithHolderDeviceState> = _uiState
    private var scannerJob: Job? = null

    init {
        _uiState.update {
            it.copy(
                isBluetoothEnabled = bluetoothAdapterProvider.isEnabled()
            )
        }
    }

    fun scanForDevice(uuid: ByteArray) {
        scannerJob = viewModelScope.launch(dispatcher) {
            if (!_uiState.value.hasAllPermissions) {
                return@launch
            }

            try {
                withTimeout(SCAN_PERIOD) {
                    when (val scanResult = scanner.scan(uuid).first()) {
                        is ScanEvent.DeviceFound -> {
                            logger.debug(
                                logTag,
                                "Bluetooth device found: ${scanResult.deviceAddress}"
                            )
                        }

                        is ScanEvent.ScanFailed -> {
                            _uiState.update {
                                it.copy(showErrorScreen = true)
                            }
                            logger.debug(logTag, "Scan failed: ${scanResult.failure}")
                        }
                    }
                }
            } catch (exception: TimeoutCancellationException) {
                logger.debug(
                    logTag,
                    "$exception"
                )
            }
        }
    }

    fun updatePermissions(hasAllPerms: Boolean) {
        _uiState.update {
            it.copy(
                hasAllPermissions = hasAllPerms
            )
        }
    }

    fun updateHasRequestPermissions(requestedPerms: Boolean) {
        _uiState.update {
            it.copy(
                hasRequestedPermissions = requestedPerms
            )
        }
    }

    fun stopScanning() {
        logger.debug(logTag, "Stopping scanner")
        scannerJob?.cancel()
    }

    override fun onCleared() {
        logger.debug(logTag, "VM cleared, stopping scanner")
        scannerJob?.cancel()
        super.onCleared()
    }

    companion object {
        const val SCAN_PERIOD = 15_000L
    }
}
