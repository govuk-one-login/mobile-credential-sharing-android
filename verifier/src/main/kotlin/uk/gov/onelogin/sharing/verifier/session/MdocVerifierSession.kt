package uk.gov.onelogin.sharing.verifier.session

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

    /**
     * gattClientManager.connect(bluetoothDevice, serviceUuid)
     */
    override fun start(serviceId: UUID) {
        logger.debug(logTag, "Starting session")
        _state.value = VerifierSessionState.Verifying
    }

    override fun stop() {
        logger.debug(logTag, "Stop session")
        _state.value = VerifierSessionState.VerifyingStopped
    }

    private fun handleGattClientEvents(event: GattClientEvent) {
        when (event) {
            is GattClientEvent.ServicesDiscovered -> {
                when (val validationResult = serviceValidator.validate(event.service)) {
                    ValidationResult.Success -> {
                        _state.value = VerifierSessionState.ServiceDiscovered
                    }

                    is ValidationResult.Failure -> {
                        _state.value = VerifierSessionState.Error(
                            validationResult.errors.toString()
                        )
                    }
                }
            }

            else -> {
                logger.debug(logTag, "Unhandled event: $event")
                _state.value = VerifierSessionState.Error("Unhandled event: $event")
            }
        }
    }
}
