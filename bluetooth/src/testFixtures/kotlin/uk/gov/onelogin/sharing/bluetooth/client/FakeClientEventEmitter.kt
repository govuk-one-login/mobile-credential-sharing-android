package uk.gov.onelogin.sharing.bluetooth.client

import uk.gov.onelogin.sharing.bluetooth.internal.client.GattClientEventEmitter
import uk.gov.onelogin.sharing.bluetooth.internal.client.GattEvent

internal class FakeClientEventEmitter : GattClientEventEmitter {
    val events = mutableListOf<GattEvent>()

    override fun emit(event: GattEvent) {
        events.add(event)
    }

}