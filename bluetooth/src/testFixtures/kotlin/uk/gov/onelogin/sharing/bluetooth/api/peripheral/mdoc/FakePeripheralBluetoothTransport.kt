package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePeripheralBluetoothTransport(
    initialState: PeripheralBluetoothState = PeripheralBluetoothState.Idle
) : PeripheralBluetoothTransport {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<PeripheralBluetoothState> = _state

    var startCalls = 0
    var stopCalls = 0
    var lastStopSendEndCommand: Boolean? = null
    var lastUuid: UUID? = null

    override suspend fun start(serviceUuid: UUID) {
        startCalls++
        lastUuid = serviceUuid
    }

    override suspend fun stop(serviceUuid: UUID, sendEndCommand: Boolean) {
        stopCalls++
        lastStopSendEndCommand = sendEndCommand
    }

    fun emitState(state: PeripheralBluetoothState) {
        _state.value = state
    }

    override suspend fun notifySessionEnd(serviceUuid: UUID) {
        lastUuid = serviceUuid
    }

    var sendMessageResult: Boolean = true
    var sendMessageCalls: Int = 0

    var sendMessageResultDeferred: CompletableDeferred<Boolean>? = null

    override suspend fun sendMessage(serviceUuid: UUID, data: ByteArray): Boolean {
        sendMessageCalls++
        return sendMessageResultDeferred?.await() ?: sendMessageResult
    }
}
