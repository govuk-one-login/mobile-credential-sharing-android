package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent

interface GattServerManager : AutoCloseable {
    val events: SharedFlow<GattServerEvent>

    fun open(serviceUuid: UUID)
}
