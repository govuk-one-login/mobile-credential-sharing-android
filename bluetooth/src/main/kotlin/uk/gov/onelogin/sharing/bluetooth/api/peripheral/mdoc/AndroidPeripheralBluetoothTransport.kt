package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
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
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(scope = AppScope::class)
class AndroidPeripheralBluetoothTransport(
    private val bleAdvertiser: BleAdvertiser,
    private val gattServerManager: GattServerManager,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    @ApplicationScope coroutineScope: CoroutineScope,
    private val logger: Logger
) : PeripheralBluetoothTransport {
    private val _state = MutableStateFlow<PeripheralBluetoothState>(PeripheralBluetoothState.Idle)
    override val state: StateFlow<PeripheralBluetoothState> = _state

    private val _bluetoothStatus = MutableStateFlow(BluetoothStatus.UNKNOWN)
    override val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus

    private val connectedDevices = mutableSetOf<String>()

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

        bluetoothStateMonitor.start()
    }

    override suspend fun start(serviceUuid: UUID) {
        try {
            bleAdvertiser.startAdvertise(BleAdvertiseData(serviceUuid))
        } catch (e: StartAdvertisingException) {
            logger.error(logTag, "Error starting advertising: ${e.error}", e)
            _state.value =
                PeripheralBluetoothState.Error(PeripheralBluetoothTransportError.ADVERTISING_FAILED)
        }

        gattServerManager.open(serviceUuid)
    }

    override suspend fun stop() {
        bleAdvertiser.stopAdvertise()
        gattServerManager.close()
        bluetoothStateMonitor.stop()
    }

    override fun notifySessionEnd(serviceUuid: UUID) {
        gattServerManager.notifySessionEnd(serviceUuid)
    }

    private fun handleAdvertiserState(state: AdvertiserState) {
        when (state) {
            AdvertiserState.Started ->
                _state.value = PeripheralBluetoothState.AdvertisingStarted

            AdvertiserState.Stopped ->
                _state.value = PeripheralBluetoothState.AdvertisingStopped

            is AdvertiserState.Failed ->
                _state.value =
                    PeripheralBluetoothState.Error(
                        PeripheralBluetoothTransportError.ADVERTISING_FAILED
                    )

            AdvertiserState.Idle ->
                _state.value = PeripheralBluetoothState.Idle

            else -> Unit
        }
    }

    private fun handleGattEvent(event: GattServerEvent) {
        when (event) {
            is GattServerEvent.Connected -> {
                if (connectedDevices.add(event.address)) {
                    _state.value = PeripheralBluetoothState.Connected(event.address)
                }
            }

            is GattServerEvent.Disconnected -> {
                if (connectedDevices.remove(event.address)) {
                    _state.value =
                        PeripheralBluetoothState.Disconnected(event.address, event.isSessionEnd)
                }
            }

            is GattServerEvent.Error ->
                _state.value = PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.fromGattError(event.error)
                )

            is GattServerEvent.ServiceAdded ->
                _state.value = PeripheralBluetoothState.ServiceAdded(event.service?.uuid)

            GattServerEvent.ServiceStopped ->
                _state.value = PeripheralBluetoothState.GattServiceStopped

            is GattServerEvent.UnsupportedEvent ->
                logger.error(
                    logTag,
                    "Mdoc - Unsupported event - status: ${event.status} new state: ${event.newState}"
                )

            GattServerEvent.SessionStarted -> {
                logger.debug(
                    logTag,
                    "Mdoc - Connection has been setup successfully - session state started"
                )
            }

            is GattServerEvent.SessionEnd -> {
                _state.value = PeripheralBluetoothState.PeripheralBluetoothEnded(event.status)
                logger.debug(
                    logTag,
                    "Mdoc - Session end command was received. Closing connection"
                )
            }

            is GattServerEvent.MessageReceived -> {
                _state.value = PeripheralBluetoothState.MessageReceived(event.byteArray)
            }
        }
    }
}
