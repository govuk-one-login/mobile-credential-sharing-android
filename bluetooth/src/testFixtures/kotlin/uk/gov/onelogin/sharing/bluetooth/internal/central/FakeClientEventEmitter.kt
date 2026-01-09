package uk.gov.onelogin.sharing.bluetooth.internal.central

internal class FakeClientEventEmitter : GattClientEventEmitter {
    val events = mutableListOf<GattEvent>()

    override fun emit(event: GattEvent) {
        events.add(event)
    }
}
