package uk.gov.onelogin.sharing.bluetooth.api.gatt.server

import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow

interface GattServerManager : AutoCloseable {
    val events: SharedFlow<GattServerEvent>

    fun open(serviceUuid: UUID)
}
