package uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral

import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow

interface GattServerManager {
    val events: SharedFlow<GattServerEvent>

    fun open(serviceUuid: UUID)

    fun close()
}
