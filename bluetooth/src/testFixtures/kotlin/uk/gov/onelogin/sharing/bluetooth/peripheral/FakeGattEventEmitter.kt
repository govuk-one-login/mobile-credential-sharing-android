package uk.gov.onelogin.sharing.bluetooth.peripheral

import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattEvent
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattEventEmitter

class FakeGattEventEmitter : GattEventEmitter {
    val events = mutableListOf<GattEvent>()
    override fun emit(event: GattEvent) {
        events.add(event)
    }
}
