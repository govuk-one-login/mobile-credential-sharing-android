@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
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
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.permissions.isPermanentlyDenied
import uk.gov.onelogin.sharing.core.Receiver
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.ConnectToDevice
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.RequestedPermission
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.StartScanning
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.StopScanning
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.UpdateEngagementData
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.UpdatePermission
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionFactory
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionState

@Inject
@ViewModelKey(SessionEstablishmentViewModel::class)
@ContributesIntoMap(ViewModelScope::class, binding = binding<ViewModel>())
class SessionEstablishmentViewModel(
    private val bluetoothAdapterProvider: BluetoothAdapterProvider,
    verifierSessionFactory: VerifierSessionFactory,
    private val scanner: BluetoothScanner,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: Logger,
    private val bluetoothStatusMonitor: BluetoothStateMonitor
) : ViewModel(),
    Receiver<ConnectWithHolderDeviceEvent> {

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

    private fun connect(device: BluetoothDevice, serviceUuid: ByteArray) {
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

    /**
     * @see connect
     * @see updateHasRequestPermissions
     * @see scanForDevice
     * @see stopScanning
     * @see updateEngagementData
     * @see updatePermissions
     */
    override fun receive(event: ConnectWithHolderDeviceEvent) = when (event) {
        is ConnectToDevice ->
            connect(event.device, event.serviceUuid)

        is RequestedPermission ->
            updateHasRequestPermissions(event.hasRequestedPermission)

        is StartScanning ->
            scanForDevice(event.uuid)

        is UpdateEngagementData ->
            updateEngagementData(event.base64EncodedEngagement)

        is UpdatePermission ->
            updatePermissions(event.state)

        StopScanning ->
            stopScanning()
    }

    private fun scanForDevice(uuid: ByteArray) {
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

                            receive(
                                ConnectToDevice(
                                    scanResult.device,
                                    uuid
                                )
                            )
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

    private fun stopScanning() {
        if (scannerJob?.isActive == true) {
            logger.debug(logTag, "Terminating session")
            scannerJob?.cancel()
        }
    }

    override fun onCleared() {
        logger.debug(logTag, "VM cleared, stopping scanner")
        receive(StopScanning)
        super.onCleared()
    }

    private fun updateEngagementData(base64EncodedEngagement: String) {
        updateState {
            it.copy(
                base64EncodedEngagement = base64EncodedEngagement,
                engagementData = decodeDeviceEngagement(base64EncodedEngagement, logger)
            )
        }
    }

    private fun updateHasRequestPermissions(requestedPerms: Boolean) {
        updateState {
            it.copy(
                hasRequestedPermissions = requestedPerms
            )
        }
    }

    private fun updatePermissions(state: MultiplePermissionsState) {
        updateState {
            it.copy(hasAllPermissions = state.allPermissionsGranted)
        }
        when {
            state.allPermissionsGranted -> "All required Bluetooth permissions have been granted"
            state.isPermanentlyDenied() -> "Bluetooth permissions were permanently denied"
            else -> "Bluetooth permissions were denied"
        }.let { logger.debug(logTag, it) }
    }

    fun updateState(updatedState: (ConnectWithHolderDeviceState) -> ConnectWithHolderDeviceState) {
        _uiState.update(updatedState)
    }

    companion object {
        const val SCAN_PERIOD = 15_000L
    }
}
