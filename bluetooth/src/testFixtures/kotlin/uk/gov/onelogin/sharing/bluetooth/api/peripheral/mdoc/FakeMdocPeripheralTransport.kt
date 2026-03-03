package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus

class FakeMdocPeripheralTransport(initialState: MdocPeripheralState = MdocPeripheralState.Idle) :
    MdocPeripheralTransport {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<MdocPeripheralState> = _state

    private val _bluetoothStatus = MutableStateFlow(BluetoothStatus.UNKNOWN)
    override val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus

    var startCalls = 0
    var stopCalls = 0
    var lastUuid: UUID? = null
    var mockBluetoothEnabled: Boolean = true

    override suspend fun start(serviceUuid: UUID) {
        startCalls++
        lastUuid = serviceUuid
        _state.value = MdocPeripheralState.AdvertisingStarted
    }

    override suspend fun stop() {
        stopCalls++
        _state.value = MdocPeripheralState.AdvertisingStopped
    }

    fun emitState(state: MdocPeripheralState) {
        _state.value = state
    }

    fun emitBluetoothState(state: BluetoothStatus) {
        _bluetoothStatus.value = state
    }

    override fun notifySessionEnd(serviceUuid: UUID) {
        lastUuid = serviceUuid
    }
}
