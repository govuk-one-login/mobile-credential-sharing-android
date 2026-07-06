package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeCentralBluetoothTransport(
    initialState: CentralBluetoothState = CentralBluetoothState.Idle
) : CentralBluetoothTransport {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<CentralBluetoothState> = _state
    override var isBleOpen: Boolean = false
    var scanAndConnectCalls = 0
    var stopCalls = 0
    var sendEndCalls = 0
    var lastServiceUuid: UUID? = null
    var sendMessageToReturn: Boolean = true
    var lastSentData: ByteArray? = null

    override suspend fun start(serviceUuid: UUID) {
        scanAndConnectCalls++
        lastServiceUuid = serviceUuid
    }

    override suspend fun sendEnd() {
        sendEndCalls++
    }

    override suspend fun stop() {
        stopCalls++
    }

    override suspend fun sendMessage(serviceUuid: UUID, data: ByteArray): Boolean {
        lastSentData = data
        return sendMessageToReturn
    }

    fun emitState(state: CentralBluetoothState) {
        when (state) {
            is CentralBluetoothState.Connected,
            is CentralBluetoothState.ConnectionStateStarted -> isBleOpen = true

            is CentralBluetoothState.Disconnected,
            is CentralBluetoothState.Error,
            is CentralBluetoothState.CentralBluetoothEnded -> isBleOpen = false

            else -> {}
        }
        _state.value = state
    }
}
