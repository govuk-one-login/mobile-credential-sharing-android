package uk.gov.onelogin.sharing.bluetooth.peripheral

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattServerManager

class FakeGattServerManager : GattServerManager {
    private val _events = MutableSharedFlow<GattServerEvent>()
    override val events: SharedFlow<GattServerEvent> = _events

    var startCalls = 0
    var stopCalls = 0

    override fun open() {
        startCalls++
    }

    override fun close() {
        stopCalls++
    }

    fun emitEvent(event: GattServerEvent) {
        _events.tryEmit(event)
    }
}
