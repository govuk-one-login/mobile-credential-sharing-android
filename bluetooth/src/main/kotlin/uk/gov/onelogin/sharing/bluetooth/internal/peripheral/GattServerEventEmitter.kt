package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

internal fun interface GattServerEventEmitter {
    fun emit(event: GattServerCallbackEvent)
}
