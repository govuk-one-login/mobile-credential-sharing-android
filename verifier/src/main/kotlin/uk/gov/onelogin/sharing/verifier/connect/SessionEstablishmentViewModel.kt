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
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import uk.gov.logging.api.Logger
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.core.Receiver
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.presentation.permissions.isPermanentlyDenied
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.ConnectToDevice
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.RequestedPermission
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.StartScanning
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.StopScanning
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.UpdateEngagementData
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEvent.UpdatePermission
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionFactory
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verifier.verify.VerifyCredentialViewModel

@Suppress("LongParameterList")
@AssistedInject
class SessionEstablishmentViewModel(
    private val bluetoothAdapterProvider: BluetoothAdapterProvider,
    verifierSessionFactory: VerifierSessionFactory,
    private val scanner: BluetoothScanner,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: Logger,
    private val bluetoothStatusMonitor: BluetoothStateMonitor,
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val orchestrator: Orchestrator.Verifier
) : ViewModel(),
    Receiver<ConnectWithHolderDeviceEvent> {
    private val initialState = ConnectWithHolderDeviceState(
        previouslyHadPermissions = savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] ?: false
    )
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<ConnectWithHolderDeviceState> = _uiState

    private val _navEvents = MutableSharedFlow<ConnectWithHolderDeviceNavEvent>(
        extraBufferCapacity = 1
    )
    val navEvents: SharedFlow<ConnectWithHolderDeviceNavEvent> = _navEvents

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
        updateState {
            it.copy(
                isBluetoothEnabled = bluetoothAdapterProvider.isEnabled()
            )
        }

        viewModelScope.launch {
            mdocVerifierSession.state.collect { sessionState ->
                when (sessionState) {
                    VerifierSessionState.Invalid,
                    VerifierSessionState.ServiceNotFound
                    ->
                        _navEvents.tryEmit(
                            ConnectWithHolderDeviceNavEvent.NavigateToError(
                                ConnectWithHolderDeviceError.BluetoothConfigurationError
                            )
                        )

                    is VerifierSessionState.Error ->
                        _navEvents.tryEmit(
                            ConnectWithHolderDeviceNavEvent.NavigateToError(
                                ConnectWithHolderDeviceError.GenericError
                            )
                        )

                    is VerifierSessionState.Connected ->
                        updateState {
                            it.copy(
                                connectionStateStarted = true
                            )
                        }

                    is VerifierSessionState.Disconnected -> {
                        val started = _uiState.value.connectionStateStarted
                        if (started) {
                            mdocVerifierSession.stop()
                        }
                        if (sessionState.isSessionEnd) {
                            logger.debug(
                                logTag,
                                "BLE session terminated successfully via GATT End command"
                            )
                        } else {
                            _navEvents.tryEmit(
                                ConnectWithHolderDeviceNavEvent.NavigateToError(
                                    ConnectWithHolderDeviceError.BluetoothConnectionError
                                )
                            )
                        }
                    }

                    is VerifierSessionState.ConnectionStateStarted -> Unit

                    else -> Unit
                }

                logger.debug(logTag, "Session state: $sessionState")
            }
        }

        bluetoothStatusMonitor.start()
        viewModelScope.launch {
            bluetoothStatusMonitor.states.collect { bluetoothState ->
                when (bluetoothState) {
                    BluetoothStatus.ON,
                    BluetoothStatus.TURNING_ON
                    -> {
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

                        _navEvents.tryEmit(
                            ConnectWithHolderDeviceNavEvent.NavigateToError(
                                ConnectWithHolderDeviceError.BluetoothDisabledError
                            )
                        )

                        mdocVerifierSession.stop()

                        logger.debug(logTag, "Bluetooth turned off during session")
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun connect(device: BluetoothDevice, serviceUuid: ByteArray) {
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
    @OptIn(ExperimentalPermissionsApi::class)
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
                            _navEvents.tryEmit(
                                ConnectWithHolderDeviceNavEvent.NavigateToError(
                                    ConnectWithHolderDeviceError.GenericError
                                )
                            )
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
        mdocVerifierSession.stop()
        receive(StopScanning)
        super.onCleared()
    }

    fun resetOrchestrator() {
        orchestrator.cancel()
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
        val hadPermissionsPreviously = _uiState.value.previouslyHadPermissions
        val hasAllPerms = state.allPermissionsGranted
        val shouldShowError = hadPermissionsPreviously && !hasAllPerms
        val grantedPermissionsForFirstTime = !hadPermissionsPreviously && hasAllPerms

        updateState {
            it.copy(hasAllPermissions = hasAllPerms)
        }
        when {
            hasAllPerms -> "All required Bluetooth permissions have been granted"
            state.isPermanentlyDenied() -> "Bluetooth permissions were permanently denied"
            else -> "Bluetooth permissions were denied"
        }.let { logger.debug(logTag, it) }

        if (grantedPermissionsForFirstTime) {
            savedStateHandle[PREVIOUSLY_HAD_PERMISSIONS_KEY] = true
        }

        if (shouldShowError) {
            logger.debug(logTag, "Bluetooth app permissions revoked during session")
            _navEvents.tryEmit(
                ConnectWithHolderDeviceNavEvent.NavigateToError(
                    ConnectWithHolderDeviceError.BluetoothPermissionsError
                )
            )

            mdocVerifierSession.stop()
        }
    }

    private fun updateState(
        updatedState: (ConnectWithHolderDeviceState) -> ConnectWithHolderDeviceState
    ) {
        _uiState.update(updatedState)
    }

    companion object {
        const val SCAN_PERIOD = 15_000L
        const val PREVIOUSLY_HAD_PERMISSIONS_KEY = "previouslyHadPermissions"
    }
}
