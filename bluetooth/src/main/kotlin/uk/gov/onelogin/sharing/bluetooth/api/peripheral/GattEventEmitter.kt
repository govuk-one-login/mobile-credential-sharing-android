package uk.gov.onelogin.sharing.bluetooth.api.peripheral

fun interface GattEventEmitter {
    fun emit(event: GattEvent)
}
