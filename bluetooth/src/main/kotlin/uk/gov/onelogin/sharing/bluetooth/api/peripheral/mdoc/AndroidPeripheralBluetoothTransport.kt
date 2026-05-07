package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.MessageSender
import uk.gov.onelogin.sharing.bluetooth.internal.core.BLE_SEND_NOTIFICATION_DELAY
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(scope = AppScope::class, binding = binding<PeripheralBluetoothTransport>())
class AndroidPeripheralBluetoothTransport(
    private val bleAdvertiser: BleAdvertiser,
    private val gattServerManager: GattServerManager,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    @ApplicationScope coroutineScope: CoroutineScope,
    private val logger: Logger
) : PeripheralBluetoothTransport,
    MessageSender by gattServerManager {

    private val _state = MutableStateFlow<PeripheralBluetoothState>(PeripheralBluetoothState.Idle)
    override val state: StateFlow<PeripheralBluetoothState> = _state

    private val _bluetoothStatus = MutableStateFlow(BluetoothStatus.UNKNOWN)
    override val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus

    init {
        coroutineScope.launch {
            bleAdvertiser.state.collect {
                handleAdvertiserState(it)
            }
        }

        coroutineScope.launch {
            gattServerManager.events.collect {
                handleGattEvent(it)
            }
        }

        coroutineScope.launch {
            bluetoothStateMonitor.states.collect { state ->
                when (state) {
                    BluetoothStatus.OFF,
                    BluetoothStatus.TURNING_OFF -> {
                        bleAdvertiser.stopAdvertise()
                        gattServerManager.close()
                        _bluetoothStatus.value = BluetoothStatus.OFF
                    }

                    BluetoothStatus.ON -> {
                        _bluetoothStatus.value = BluetoothStatus.ON
                    }

                    else -> Unit
                }
            }
        }
    }

    override suspend fun start(serviceUuid: UUID) {
        bluetoothStateMonitor.start()
        try {
            bleAdvertiser.startAdvertise(BleAdvertiseData(serviceUuid))
        } catch (e: StartAdvertisingException) {
            logger.error(logTag, "Error starting advertising: ${e.error}", e)
            _state.value =
                PeripheralBluetoothState.Error(PeripheralBluetoothTransportError.ADVERTISING_FAILED)
        }

        gattServerManager.open(serviceUuid)
    }

    override suspend fun stop(serviceUuid: UUID, sendEndCommand: Boolean) {
        if (sendEndCommand) {
            notifySessionEnd(serviceUuid)
        }
        bleAdvertiser.stopAdvertise()
        gattServerManager.close()
        bluetoothStateMonitor.stop()
    }

    override suspend fun notifySessionEnd(serviceUuid: UUID) {
        val result = gattServerManager.notifySessionEnd(serviceUuid)
        if (result == SessionEndStateQueued.Success) {
            // allow time for the END notification to be sent before closing the GATT server
            delay(BLE_SEND_NOTIFICATION_DELAY)
        }
    }

    private fun handleAdvertiserState(state: AdvertiserState) {
        when (state) {
            AdvertiserState.Started ->
                logger.debug(logTag, "Advertising Started")

            AdvertiserState.Stopped ->
                logger.debug(logTag, "Advertising Stopped")

            is AdvertiserState.Failed ->
                _state.value =
                    PeripheralBluetoothState.Error(
                        PeripheralBluetoothTransportError.ADVERTISING_FAILED
                    )

            AdvertiserState.Idle ->
                logger.debug(logTag, "Idle")

            else -> Unit
        }
    }

    private fun handleGattEvent(event: GattServerEvent) {
        when (event) {
            is GattServerEvent.Connected ->
                _state.value = PeripheralBluetoothState.Connected(event.address)

            is GattServerEvent.Disconnected ->
                _state.value =
                    PeripheralBluetoothState.Disconnected(event.address, event.isSessionEnd)

            is GattServerEvent.Error ->
                _state.value = PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.fromGattError(event.error)
                )

            is GattServerEvent.ServiceAdded ->
                logger.debug(logTag, "Service Added: ${event.service?.uuid}")

            GattServerEvent.ServiceStopped ->
                logger.debug(logTag, "GattService Stopped")

            is GattServerEvent.UnsupportedEvent ->
                logger.error(
                    logTag,
                    "Unsupported event - status: ${event.status} new state: ${event.newState}"
                )

            GattServerEvent.SessionStarted ->
                logger.debug(
                    logTag,
                    "Connection has been setup successfully - session state started"
                )

            is GattServerEvent.SessionEnd -> {
                _state.value = PeripheralBluetoothState.Ended(event.status)
                logger.debug(
                    logTag,
                    "Session end command was received. Closing connection"
                )
            }

            is GattServerEvent.MessageReceived ->
                _state.value = PeripheralBluetoothState.MessageReceived(event.byteArray)
        }
    }
}
