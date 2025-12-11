package uk.gov.onelogin.sharing.bluetooth.peripheral

import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattServerManager

class FakeGattServerManager : GattServerManager {
    private val _events = MutableSharedFlow<GattServerEvent>()
    override val events: SharedFlow<GattServerEvent> = _events

    var openCalls = 0
    var closeCalls = 0

    override fun open(serviceUuid: UUID) {
        openCalls++
    }

    override fun close() {
        closeCalls++
    }

    suspend fun emitEvent(event: GattServerEvent) {
        _events.emit(event)
    }
}
