package uk.gov.onelogin.sharing.bluetooth.internal

import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.bluetooth.api.MdocError
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattServerManager

internal class AndroidMdocSessionManager(
    private val bleAdvertiser: BleAdvertiser,
    private val gattServerManager: GattServerManager,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : MdocSessionManager {
    private val _state = MutableStateFlow<MdocSessionState>(MdocSessionState.Idle)
    override val state: StateFlow<MdocSessionState> = _state
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
    }

    override suspend fun start(serviceUuid: UUID) {
        try {
            bleAdvertiser.startAdvertise(BleAdvertiseData(serviceUuid))
        } catch (e: StartAdvertisingException) {
            println("Error starting advertising: ${e.error}")
            _state.value = MdocSessionState.Error(MdocError.ADVERTISING_FAILED)
        }

        gattServerManager.open()
    }

    override suspend fun stop() {
        bleAdvertiser.stopAdvertise()
        _state.value = MdocSessionState.Stopped
    }

    private fun handleAdvertiserState(state: AdvertiserState) {
        when (state) {
            AdvertiserState.Started ->
                _state.value = MdocSessionState.Advertising

            AdvertiserState.Stopped ->
                _state.value = MdocSessionState.Stopped

            is AdvertiserState.Failed ->
                _state.value = MdocSessionState.Error(MdocError.ADVERTISING_FAILED)

            AdvertiserState.Idle ->
                _state.value = MdocSessionState.Idle

            else -> Unit
        }
    }

    private fun handleGattEvent(event: GattServerEvent) {
        when (event) {
            is GattServerEvent.Connected -> {
                connectedDevices.add(event.address)
                _state.value = MdocSessionState.Connected(event.address)
            }

            is GattServerEvent.Disconnected -> {
                connectedDevices.remove(event.address)
                _state.value = MdocSessionState.Disconnected(event.address)
            }

            is GattServerEvent.Error ->
                _state.value = MdocSessionState.Error(event.error)

            is GattServerEvent.UnsupportedEvent ->
                println("Ignored unsupported event: $event")
        }
    }
}
