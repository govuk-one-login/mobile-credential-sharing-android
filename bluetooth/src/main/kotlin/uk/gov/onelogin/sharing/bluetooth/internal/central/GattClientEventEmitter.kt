package uk.gov.onelogin.sharing.bluetooth.internal.central

internal fun interface GattClientEventEmitter {
    fun emit(event: GattEvent)
}
