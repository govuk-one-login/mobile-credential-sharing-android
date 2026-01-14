package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothDevice
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus

class FakeVerifierSession(initialState: VerifierSessionState = VerifierSessionState.Idle) :
    VerifierSession {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<VerifierSessionState> = _state

    override val bluetoothStatus: StateFlow<BluetoothStatus> = MutableStateFlow(BluetoothStatus.ON)

    var startCalls = 0
    var connectCalls = 0
    var stopCalls = 0

    override fun start(serviceId: UUID) {
        startCalls++
    }

    override fun connect(device: BluetoothDevice, serviceUuid: UUID) {
        connectCalls++
    }

    override fun stop() {
        stopCalls++
    }

    fun emitState(state: VerifierSessionState) {
        _state.value = state
    }
}
