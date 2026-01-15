@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
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
import uk.gov.onelogin.sharing.bluetooth.BluetoothUiErrorTypes
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.permissions.isPermanentlyDenied
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionFactory
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionState

@AssistedInject
class SessionEstablishmentViewModel(
    private val bluetoothAdapterProvider: BluetoothAdapterProvider,
    verifierSessionFactory: VerifierSessionFactory,
    private val scanner: BluetoothScanner,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: Logger,
    private val bluetoothStatusMonitor: BluetoothStateMonitor,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val initialState = ConnectWithHolderDeviceState(
        previouslyHadPermissions = savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] ?: false
    )
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<ConnectWithHolderDeviceState> = _uiState
    private var scannerJob: Job? = null
    val mdocVerifierSession = verifierSessionFactory.create(viewModelScope)

    @AssistedFactory
    @ViewModelAssistedFactoryKey(SessionEstablishmentViewModel::class)
    @ContributesIntoMap(ViewModelScope::class)
    interface Factory : ViewModelAssistedFactory {
        fun create(@Assisted savedStateHandle: SavedStateHandle): SessionEstablishmentViewModel

        override fun create(extras: CreationExtras): SessionEstablishmentViewModel {
            val savedStateHandle = extras.createSavedStateHandle()
            return create(savedStateHandle)
        }
    }

    init {
        _uiState.update {
            it.copy(
                isBluetoothEnabled = bluetoothAdapterProvider.isEnabled()
            )
        }

        bluetoothStatusMonitor.start()
        viewModelScope.launch {
            bluetoothStatusMonitor.states.collect { bluetoothState ->
                when (bluetoothState) {
                    BluetoothStatus.TURNING_ON -> {
                        _uiState.update {
                            it.copy(
                                isBluetoothEnabled = true
                            )
                        }
                        logger.debug(logTag, "Device bluetooth was enabled")
                    }

                    BluetoothStatus.OFF -> {
                        _uiState.update {
                            it.copy(
                                isBluetoothEnabled = false,
                                showErrorScreen = true,
                                bluetoothErrorType = BluetoothUiErrorTypes.BLUETOOTH_TURNED_OFF
                            )
                        }
                        mdocVerifierSession.stop()
                        logger.debug(logTag, "Bluetooth turned off")
                    }

                    else -> Unit
                }
            }
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
            mdocVerifierSession.state.collect { state ->
                logger.debug(logTag, "Session state: $state")
                when (state) {
                    VerifierSessionState.ConnectionStateStarted -> {
                        _uiState.update {
                            it.copy(
                                connectionStateStarted = true
                            )
                        }
                    }

                    is VerifierSessionState.Disconnected -> {
                        _uiState.update {
                            it.copy(
                                showErrorScreen = true,
                                bluetoothErrorType = BluetoothUiErrorTypes.BLUETOOTH_DISCONNECTED
                            )
                        }
                        if (_uiState.value.connectionStateStarted) {
                            mdocVerifierSession.stop()
                        }
                    }

                    else -> Unit
                }
            }
        }

        mdocVerifierSession.connect(device, serviceUuid.toUUID())
    }

    fun updatePermissions(hasAllPerms: Boolean) {
        val hadPermissionsPreviously = _uiState.value.previouslyHadPermissions
        val shouldShowError = hadPermissionsPreviously && !hasAllPerms
        val grantedPermissionsForFirstTime = !hadPermissionsPreviously && hasAllPerms

        _uiState.update {
            val next = it.copy(
                hasAllPermissions = hasAllPerms,
                previouslyHadPermissions = hadPermissionsPreviously || hasAllPerms,
            )

            if (shouldShowError) {
                next.copy(
                    showErrorScreen = true,
                    bluetoothErrorType = BluetoothUiErrorTypes.PERMISSIONS_MISSING
                )
            } else {
                next
            }
        }

        if (shouldShowError) {
            logger.debug(logTag, "Error - Permissions were revoked during the session")
            stopScanning()
            mdocVerifierSession.stop()
        }

        if (grantedPermissionsForFirstTime) {
            savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] = true
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
        private const val PREVIOUSLY_HAD_PERMISSIONS_KEY = "previouslyHadPermissions"
        const val SCAN_PERIOD = 15_000L
    }
}
