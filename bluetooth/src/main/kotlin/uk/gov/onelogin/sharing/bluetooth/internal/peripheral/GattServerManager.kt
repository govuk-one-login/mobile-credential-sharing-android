package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent

interface GattServerManager {
    val events: SharedFlow<GattServerEvent>

    fun open()

    fun close()
}
