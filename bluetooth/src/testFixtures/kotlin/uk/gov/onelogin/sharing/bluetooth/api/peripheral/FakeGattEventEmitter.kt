package uk.gov.onelogin.sharing.bluetooth.api.peripheral

class FakeGattEventEmitter : GattEventEmitter {
    val events = mutableListOf<GattServerCallbackEvent>()
    override fun emit(event: GattServerCallbackEvent) {
        events.add(event)
    }
}
