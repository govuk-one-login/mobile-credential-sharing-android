@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
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
import uk.gov.onelogin.sharing.bluetooth.permissions.isPermanentlyDenied
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionFactory

@Inject
@ViewModelKey(SessionEstablishmentViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class SessionEstablishmentViewModel(
    private val bluetoothAdapterProvider: BluetoothAdapterProvider,
    verifierSessionFactory: VerifierSessionFactory,
    private val scanner: BluetoothScanner,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectWithHolderDeviceState())
    val uiState: StateFlow<ConnectWithHolderDeviceState> = _uiState
    private var scannerJob: Job? = null
    val mdocVerifierSession = verifierSessionFactory.create(viewModelScope)

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
                                "Bluetooth device found: ${scanResult.device.address}"
                            )

                            connect(scanResult.device, uuid)
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

    private fun connect(device: BluetoothDevice, serviceUuid: ByteArray) {
        viewModelScope.launch(dispatcher) {
            mdocVerifierSession.state.collect {
                logger.debug(logTag, "Session state: $it")
            }
        }

        mdocVerifierSession.connect(device, serviceUuid.toUUID())
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

    fun permissionLogger(state: MultiplePermissionsState) {
        when {
            state.allPermissionsGranted -> logger.debug(
                logTag,
                "All required Bluetooth permissions have been granted"
            )

            state.isPermanentlyDenied() -> logger.debug(
                logTag,
                "Bluetooth permissions were permanently denied"
            )

            else -> {
                logger.debug(logTag, "Bluetooth permissions were denied")
            }
        }
    }

    fun stopScanning() {
        if (scannerJob?.isActive == true) {
            logger.debug(logTag, "Terminating session")
            scannerJob?.cancel()
        }
    }

    override fun onCleared() {
        logger.debug(logTag, "VM cleared, stopping scanner")
        stopScanning()
        super.onCleared()
    }

    companion object {
        const val SCAN_PERIOD = 15_000L
    }
}
