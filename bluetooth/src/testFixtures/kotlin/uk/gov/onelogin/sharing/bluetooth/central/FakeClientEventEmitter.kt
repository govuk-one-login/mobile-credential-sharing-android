package uk.gov.onelogin.sharing.bluetooth.central

import uk.gov.onelogin.sharing.bluetooth.internal.central.GattClientEventEmitter
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattEvent

internal class FakeClientEventEmitter : GattClientEventEmitter {
    val events = mutableListOf<GattEvent>()

    override fun emit(event: GattEvent) {
        events.add(event)
    }
}
