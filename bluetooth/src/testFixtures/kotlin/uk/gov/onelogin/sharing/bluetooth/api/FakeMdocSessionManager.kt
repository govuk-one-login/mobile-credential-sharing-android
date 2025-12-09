package uk.gov.onelogin.sharing.bluetooth.api

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStatus

class FakeMdocSessionManager(initialState: MdocSessionState = MdocSessionState.Idle) :
    MdocSessionManager {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<MdocSessionState> = _state

    private val _bluetoothStatus = MutableStateFlow(BluetoothStatus.UNKNOWN)
    override val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus

    var startCalls = 0
    var stopCalls = 0
    var lastUuid: UUID? = null
    var mockBluetoothEnabled: Boolean = true

    override suspend fun start(serviceUuid: UUID) {
        startCalls++
        lastUuid = serviceUuid
        _state.value = MdocSessionState.AdvertisingStarted
    }

    override suspend fun stop() {
        stopCalls++
        _state.value = MdocSessionState.AdvertisingStopped
    }

    fun emitState(state: MdocSessionState) {
        _state.value = state
    }

    fun emitBluetoothState(state: BluetoothStatus) {
        _bluetoothStatus.value = state
    }
}
