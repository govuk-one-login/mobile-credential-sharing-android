package uk.gov.onelogin.sharing.bluetooth.internal.client

internal fun interface GattClientEventEmitter {
    fun emit(event: GattEvent)
}
