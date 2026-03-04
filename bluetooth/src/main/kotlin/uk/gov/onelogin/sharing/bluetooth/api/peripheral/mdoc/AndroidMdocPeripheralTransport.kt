package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.Logger
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
class AndroidMdocPeripheralTransport(
    private val bleAdvertiser: BleAdvertiser,
    private val gattServerManager: GattServerManager,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    @ApplicationScope coroutineScope: CoroutineScope,
    private val logger: Logger
) : MdocPeripheralTransport {
    private val _state = MutableStateFlow<MdocPeripheralState>(MdocPeripheralState.Idle)
    override val state: StateFlow<MdocPeripheralState> = _state

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
                MdocPeripheralState.Error(MdocPeripheralTransportError.ADVERTISING_FAILED)
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
                _state.value = MdocPeripheralState.AdvertisingStarted

            AdvertiserState.Stopped ->
                _state.value = MdocPeripheralState.AdvertisingStopped

            is AdvertiserState.Failed ->
                _state.value =
                    MdocPeripheralState.Error(MdocPeripheralTransportError.ADVERTISING_FAILED)

            AdvertiserState.Idle ->
                _state.value = MdocPeripheralState.Idle

            else -> Unit
        }
    }

    private fun handleGattEvent(event: GattServerEvent) {
        when (event) {
            is GattServerEvent.Connected -> {
                if (connectedDevices.add(event.address)) {
                    _state.value = MdocPeripheralState.Connected(event.address)
                }
            }

            is GattServerEvent.Disconnected -> {
                if (connectedDevices.remove(event.address)) {
                    _state.value =
                        MdocPeripheralState.Disconnected(event.address, event.isSessionEnd)
                }
            }

            is GattServerEvent.Error ->
                _state.value = MdocPeripheralState.Error(
                    MdocPeripheralTransportError.fromGattError(event.error)
                )

            is GattServerEvent.ServiceAdded ->
                _state.value = MdocPeripheralState.ServiceAdded(event.service?.uuid)

            GattServerEvent.ServiceStopped ->
                _state.value = MdocPeripheralState.GattServiceStopped

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
                _state.value = MdocPeripheralState.MdocPeripheralEnded(event.status)
                logger.debug(
                    logTag,
                    "Mdoc - Session end command was received. Closing connection"
                )
            }

            is GattServerEvent.MessageReceived -> {
                _state.value = MdocPeripheralState.MessageReceived(event.byteArray)
            }
        }
    }
}
