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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withTimeout
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.permissions.isPermanentlyDenied
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionFactory
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionState

@Inject
@ViewModelKey(SessionEstablishmentViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class SessionEstablishmentViewModel(
    private val bluetoothAdapterProvider: BluetoothAdapterProvider,
    verifierSessionFactory: VerifierSessionFactory,
    private val scanner: BluetoothScanner,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: Logger,
    private val bluetoothStatusMonitor: BluetoothStateMonitor
) : ViewModel() {

    private val base64EncodedEngagement = MutableStateFlow<String?>(null)

    val engagementData: StateFlow<DeviceEngagementDto?> = base64EncodedEngagement
        .map { engagement ->
            engagement?.let {
                decodeDeviceEngagement(
                    it,
                    logger = logger
                )
            }
        }.stateIn(
            viewModelScope.plus(dispatcher),
            SharingStarted.Lazily,
            null
        )

    private val _uiState = MutableStateFlow(ConnectWithHolderDeviceState())
    val uiState: StateFlow<ConnectWithHolderDeviceState> = _uiState
    private var scannerJob: Job? = null
    val mdocVerifierSession = verifierSessionFactory.create(viewModelScope)

    init {
        updateState {
            it.copy(
                isBluetoothEnabled = bluetoothAdapterProvider.isEnabled()
            )
        }

        bluetoothStatusMonitor.start()
        viewModelScope.launch {
            bluetoothStatusMonitor.states.collect { bluetoothState ->
                when (bluetoothState) {
                    BluetoothStatus.ON,
                    BluetoothStatus.TURNING_ON -> {
                        updateState {
                            it.copy(
                                isBluetoothEnabled = true
                            )
                        }
                        logger.debug(logTag, "Device bluetooth was enabled")
                    }

                    BluetoothStatus.OFF -> {
                        updateState {
                            it.copy(
                                isBluetoothEnabled = false
                            )
                        }
                        logger.debug(logTag, "Bluetooth turned off")
                    }

                    else -> Unit
                }
            }
        }
    }

    fun scanForDevice(uuid: ByteArray) {
        scannerJob = viewModelScope.launch(dispatcher) {
            if (!uiState.value.hasAllPermissions) {
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
                            updateState {
                                it.copy(showErrorScreen = ConnectWithHolderDeviceError.GenericError)
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

    internal fun connect(device: BluetoothDevice, serviceUuid: ByteArray) {
        mdocVerifierSession.state.value.let { sessionState ->
            when (sessionState) {
                is VerifierSessionState.Invalid ->
                    ConnectWithHolderDeviceError.BluetoothConfigurationError

                is VerifierSessionState.Error ->
                    ConnectWithHolderDeviceError.GenericError

                else -> ConnectWithHolderDeviceError.NoError
            }.let { error ->
                updateState { it.copy(showErrorScreen = error) }
            }

            logger.debug(logTag, "Session state: $sessionState")
        }

        mdocVerifierSession.connect(device, serviceUuid.toUUID())
    }

    fun updatePermissions(hasAllPerms: Boolean) {
        updateState {
            it.copy(
                hasAllPermissions = hasAllPerms
            )
        }
    }

    fun updateHasRequestPermissions(requestedPerms: Boolean) {
        updateState {
            it.copy(
                hasRequestedPermissions = requestedPerms
            )
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

    fun update(base64EncodedEngagement: String) {
        this.base64EncodedEngagement.update { base64EncodedEngagement }
    }

    fun update(state: MultiplePermissionsState) {
        updatePermissions(state.allPermissionsGranted)
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

    fun updateState(updatedState: (ConnectWithHolderDeviceState) -> ConnectWithHolderDeviceState) {
        _uiState.update(updatedState)
    }

    companion object {
        const val SCAN_PERIOD = 15_000L
    }
}
