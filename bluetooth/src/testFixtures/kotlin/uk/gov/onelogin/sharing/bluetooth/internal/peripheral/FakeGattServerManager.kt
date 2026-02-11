package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates.NOTIFY_CLIENT_FAILED
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates.SUCCESS

class FakeGattServerManager : GattServerManager {
    private val _events = MutableSharedFlow<GattServerEvent>()
    override val events: SharedFlow<GattServerEvent> = _events

    var openCalls = 0
    var closeCalls = 0
    var writeSessionEnd: Boolean = true

    override fun open(serviceUuid: UUID) {
        openCalls++
    }

    override fun notifySessionEnd(serviceUuid: UUID) {
        if (writeSessionEnd) {
            _events.tryEmit(GattServerEvent.SessionEnd(SUCCESS))
        } else {
            _events.tryEmit(
                GattServerEvent.SessionEnd(NOTIFY_CLIENT_FAILED)
            )
        }
    }

    override fun close() {
        closeCalls++
    }

    suspend fun emitEvent(event: GattServerEvent) {
        _events.emit(event)
    }
}
