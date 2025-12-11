package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

internal fun interface GattEventEmitter {
    fun emit(event: GattEvent)
}
