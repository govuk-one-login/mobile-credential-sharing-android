package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothDevice
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(AppScope::class)
class MdocVerifierSession(
    private val gattClientManager: GattClientManager,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
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

        gattClientManager.notifySessionEnd()

        gattClientManager.disconnect()
    }

    private fun handleGattClientEvents(event: GattClientEvent) {
        when (event) {
            GattClientEvent.Connecting -> _state.value = VerifierSessionState.Connecting

            is GattClientEvent.Connected ->
                _state.value = VerifierSessionState.Connected(event.deviceAddress)

            is GattClientEvent.Disconnected ->
                _state.value =
                    VerifierSessionState.Disconnected(event.deviceAddress, event.isSessionEnd)

            is GattClientEvent.Error -> {
                stop()
                _state.value = when (event.error) {
                    ClientError.INVALID_SERVICE,
                    ClientError.FAILED_TO_SUBSCRIBE,
                    ClientError.FAILED_TO_START -> VerifierSessionState.Invalid

                    ClientError.SERVICE_NOT_FOUND -> VerifierSessionState.ServiceNotFound

                    else -> VerifierSessionState.Error(event.error.toString())
                }
            }

            GattClientEvent.ConnectionStateStarted -> {
                _state.value = VerifierSessionState.ConnectionStateStarted
            }

            is GattClientEvent.UnsupportedEvent -> {
                logger.debug(logTag, "Unhandled event: $event")
                _state.value = VerifierSessionState.Error("Unhandled event: $event")
            }

            is GattClientEvent.SessionEnd -> {
                gattClientManager.disconnect()
            }
        }
    }
}
