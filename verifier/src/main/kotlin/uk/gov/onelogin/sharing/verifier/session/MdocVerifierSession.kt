package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothDevice
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager
import uk.gov.onelogin.sharing.core.logger.logTag

@Inject
@ContributesBinding(ViewModelScope::class)
class MdocVerifierSession(
    private val gattClientManager: GattClientManager,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    private val serviceValidator: ServiceValidator,
    private val logger: Logger,
    scope: CoroutineScope
) : VerifierSession {
    private val _state = MutableStateFlow<VerifierSessionState>(VerifierSessionState.Idle)
    override val state: StateFlow<VerifierSessionState> = _state

    private val _bluetoothStatus = MutableStateFlow(BluetoothStatus.OFF)
    override val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus

    init {
        scope.launch {
            gattClientManager.events.collect {
                handleGattClientEvents(it)
            }
        }

        scope.launch {
            bluetoothStateMonitor.states.collect {
            }
        }
    }

    // This method is meant to start the UUID scanning and connection
    // We need to add the BLE scanner as dependency
    override fun start(serviceId: UUID) {
        logger.debug(logTag, "Starting session")
        _state.value = VerifierSessionState.Starting
    }

    // This can be removed when the BLE scanner is added as a dependency
    // The connect will be triggered after a successful scan
    override fun connect(device: BluetoothDevice, serviceUuid: UUID) {
        gattClientManager.connect(
            device = device,
            serviceUuid = serviceUuid
        )
    }

    override fun stop() {
        logger.debug(logTag, "Stop session")
        _state.value = VerifierSessionState.Stopped
    }

    private fun handleGattClientEvents(event: GattClientEvent) {
        when (event) {
            is GattClientEvent.ServicesDiscovered -> {
                when (val validationResult = serviceValidator.validate(event.service)) {
                    ValidationResult.Success ->
                        _state.value = VerifierSessionState.ServiceDiscovered

                    is ValidationResult.Failure ->
                        _state.value = VerifierSessionState.Error(
                            validationResult.errors.toString()
                        )
                }
            }

            GattClientEvent.Connecting -> _state.value = VerifierSessionState.Connecting

            is GattClientEvent.Connected ->
                _state.value = VerifierSessionState.Connected(event.deviceAddress)

            is GattClientEvent.Disconnected ->
                _state.value = VerifierSessionState.Disconnected(event.deviceAddress)

            is GattClientEvent.Error ->
                _state.value =
                    VerifierSessionState.Error(event.error.toString())

            is GattClientEvent.UnsupportedEvent -> {
                logger.debug(logTag, "Unhandled event: $event")
                _state.value = VerifierSessionState.Error("Unhandled event: $event")
            }
        }
    }
}
