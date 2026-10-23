package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

internal class FakeGattServerEventEmitter : GattServerEventEmitter {
    val events = mutableListOf<GattServerCallbackEvent>()
    override fun emit(event: GattServerCallbackEvent) {
        events.add(event)
    }
}
