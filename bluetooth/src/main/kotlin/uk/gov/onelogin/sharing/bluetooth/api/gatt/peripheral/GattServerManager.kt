package uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral

import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.SessionEndStateQueued

interface GattServerManager :
    AutoCloseable,
    MessageSender {
    val events: SharedFlow<GattServerEvent>

    fun open(serviceUuid: UUID)

    fun notifySessionEnd(serviceUuid: UUID): SessionEndStateQueued
}
