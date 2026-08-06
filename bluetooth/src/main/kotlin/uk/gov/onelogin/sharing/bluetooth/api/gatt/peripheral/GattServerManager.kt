package uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral

import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.MessageSender
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.SessionEndStateQueued

interface GattServerManager :
    AutoCloseable,
    MessageSender {
    val events: SharedFlow<GattServerEvent>

    fun open(serviceUuid: UUID)

    /**
     * Disconnects the currently connected bluetooth device.
     */
    fun cancelCurrentConnection()

    fun notifySessionEnd(serviceUuid: UUID): SessionEndStateQueued
}
