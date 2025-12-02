package uk.gov.onelogin.sharing.bluetooth.internal

import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothState
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattServerManager

internal class AndroidMdocSessionManager(
    private val bleAdvertiser: BleAdvertiser,
    private val gattServerManager: GattServerManager,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    coroutineScope: CoroutineScope
) : MdocSessionManager {
    private val _state = MutableStateFlow<MdocSessionState>(MdocSessionState.Idle)
    override val state: StateFlow<MdocSessionState> = _state

    private val _bluetoothState = MutableStateFlow(BluetoothState.UNKNOWN)
    override val bluetoothState: StateFlow<BluetoothState> = _bluetoothState

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
                    BluetoothState.OFF,
                    BluetoothState.TURNING_OFF -> {
                        bleAdvertiser.stopAdvertise()
                        gattServerManager.close()
                        _bluetoothState.value = BluetoothState.OFF
                    }

                    BluetoothState.ON -> {
                        _bluetoothState.value = BluetoothState.ON
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
            println("Error starting advertising: ${e.error}")
            _state.value = MdocSessionState.Error(MdocSessionError.ADVERTISING_FAILED)
        }

        gattServerManager.open(serviceUuid)
    }

    override suspend fun stop() {
        bleAdvertiser.stopAdvertise()
        gattServerManager.close()
        bluetoothStateMonitor.stop()
    }

    private fun handleAdvertiserState(state: AdvertiserState) {
        when (state) {
            AdvertiserState.Started ->
                _state.value = MdocSessionState.AdvertisingStarted

            AdvertiserState.Stopped ->
                _state.value = MdocSessionState.AdvertisingStopped

            is AdvertiserState.Failed ->
                _state.value = MdocSessionState.Error(MdocSessionError.ADVERTISING_FAILED)

            AdvertiserState.Idle ->
                _state.value = MdocSessionState.Idle

            else -> Unit
        }
    }

    private fun handleGattEvent(event: GattServerEvent) {
        when (event) {
            is GattServerEvent.Connected -> {
                if (connectedDevices.add(event.address)) {
                    _state.value = MdocSessionState.Connected(event.address)
                }
            }

            is GattServerEvent.Disconnected -> {
                if (connectedDevices.remove(event.address)) {
                    _state.value = MdocSessionState.Disconnected(event.address)
                }
            }

            is GattServerEvent.Error ->
                _state.value = MdocSessionState.Error(event.error)

            is GattServerEvent.ServiceAdded ->
                _state.value = MdocSessionState.ServiceAdded(event.service?.uuid)

            GattServerEvent.ServiceStopped ->
                _state.value = MdocSessionState.GattServiceStopped

            is GattServerEvent.UnsupportedEvent ->
                println(
                    "Mdoc - Unsupported event - status: ${event.status} new state: ${event.newState}"
                )

            GattServerEvent.SessionStarted -> {
                println("Mdoc - Connection has been setup successfully - session state started")
            }
        }
    }

    override fun isBluetoothEnabled(): Boolean = bleAdvertiser.isBluetoothEnabled()
}
