package uk.gov.onelogin.sharing.holder.mdoc

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
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
import uk.gov.onelogin.sharing.core.logger.logTag

@Inject
@ContributesBinding(scope = ViewModelScope::class)
class AndroidMdocSessionManager(
    private val bleAdvertiser: BleAdvertiser,
    private val gattServerManager: GattServerManager,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    coroutineScope: CoroutineScope,
    private val logger: Logger
) : MdocSessionManager {
    private val _state = MutableStateFlow<MdocSessionState>(MdocSessionState.Idle)
    override val state: StateFlow<MdocSessionState> = _state

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
                _state.value = MdocSessionState.Error(
                    MdocSessionError.fromGattError(event.error)
                )

            is GattServerEvent.ServiceAdded ->
                _state.value = MdocSessionState.ServiceAdded(event.service?.uuid)

            GattServerEvent.ServiceStopped ->
                _state.value = MdocSessionState.GattServiceStopped

            is GattServerEvent.UnsupportedEvent ->
                logger.error(
                    logTag,
                    "Mdoc - UUnsupported event - status: ${event.status} new state: ${event.newState}"
                )

            GattServerEvent.SessionStarted -> {
                logger.error(
                    logTag,
                    "Mdoc - Connection has been setup successfully - session state started"
                )
            }
        }
    }
}
