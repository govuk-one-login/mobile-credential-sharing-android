package uk.gov.onelogin.sharing.bluetooth.internal

import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
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
    coroutineScope: CoroutineScope,
    private val logger: Logger
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
            logger.error(TAG, "Error starting advertising", e)
            _state.value = MdocSessionState.Error(MdocSessionError.ADVERTISING_FAILED)
        }

        gattServerManager.open(serviceUuid)
    }

    override suspend fun stop() {
        bleAdvertiser.stopAdvertise()
        gattServerManager.close()
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

            is GattServerEvent.ServiceAdded -> {
                _state.value = MdocSessionState.ServiceAdded(event.service?.uuid)
            }

            is GattServerEvent.UnsupportedEvent ->
                logger.error(
                    TAG,
                    "Unsupported event - status: ${event.status} new state: ${event.newState}"
                )
        }
    }

    override fun isBluetoothEnabled(): Boolean = bleAdvertiser.isBluetoothEnabled()

    companion object {
        private const val TAG = "AndroidMdocSessionManager"
    }
}
