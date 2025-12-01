package uk.gov.onelogin.sharing.bluetooth.ble

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothState
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStateMonitor

class FakeBluetoothStateMonitor : BluetoothStateMonitor {
    private val _states = MutableSharedFlow<BluetoothState>(replay = 1)
    override val states: Flow<BluetoothState> = _states

    var startCalls = 0
    var stopCalls = 0

    fun emit(state: BluetoothState) {
        _states.tryEmit(state)
    }

    override fun start() {
        startCalls++
    }

    override fun stop() {
        stopCalls++
    }
}
