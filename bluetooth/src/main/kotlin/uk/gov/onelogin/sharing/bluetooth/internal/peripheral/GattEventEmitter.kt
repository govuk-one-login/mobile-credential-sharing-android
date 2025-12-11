package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattEvent

internal fun interface GattEventEmitter {
    fun emit(event: GattEvent)
}
