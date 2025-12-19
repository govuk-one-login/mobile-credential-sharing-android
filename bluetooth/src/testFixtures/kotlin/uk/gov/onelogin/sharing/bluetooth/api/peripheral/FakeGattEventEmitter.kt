package uk.gov.onelogin.sharing.bluetooth.api.peripheral

class FakeGattEventEmitter : GattEventEmitter {
    val events = mutableListOf<GattEvent>()
    override fun emit(event: GattEvent) {
        events.add(event)
    }
}
