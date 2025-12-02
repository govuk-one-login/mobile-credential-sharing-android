package uk.gov.onelogin.sharing.bluetooth.ble

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStatus

class FakeBluetoothStateMonitor : BluetoothStateMonitor {
    private val _states = MutableSharedFlow<BluetoothStatus>(replay = 1)
    override val states: Flow<BluetoothStatus> = _states

    var startCalls = 0
    var stopCalls = 0

    fun emit(state: BluetoothStatus) {
        _states.tryEmit(state)
    }

    override fun start() {
        startCalls++
    }

    override fun stop() {
        stopCalls++
    }
}
