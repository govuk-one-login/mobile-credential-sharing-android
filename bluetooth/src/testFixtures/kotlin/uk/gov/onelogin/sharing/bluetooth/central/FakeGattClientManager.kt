package uk.gov.onelogin.sharing.bluetooth.central

import android.bluetooth.BluetoothDevice
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager

class FakeGattClientManager : GattClientManager {
    private val _events = MutableSharedFlow<GattClientEvent>()
    override val events: SharedFlow<GattClientEvent> = _events

    var connectCalls = 0
    var disconnectCalls = 0

    override fun connect(device: BluetoothDevice, serviceUuid: UUID) {
        connectCalls++
    }

    override fun disconnect() {
        disconnectCalls++
    }

    suspend fun emitEvent(event: GattClientEvent) {
        _events.emit(event)
    }
}
