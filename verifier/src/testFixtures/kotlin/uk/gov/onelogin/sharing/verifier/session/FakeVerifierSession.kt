package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothDevice
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus

class FakeVerifierSession(
    private val initialState: MutableStateFlow<VerifierSessionState> = MutableStateFlow(
        VerifierSessionState.Idle
    )
) : VerifierSession {
    override val state: StateFlow<VerifierSessionState> =
        initialState
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

    fun updateState(state: VerifierSessionState) {
        initialState.update { state }
    }
}
