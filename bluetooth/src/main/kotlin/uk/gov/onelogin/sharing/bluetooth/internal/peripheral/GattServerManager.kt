package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.MdocEvent

interface GattServerManager {
    val events: SharedFlow<MdocEvent>

    fun open()

    fun close()
}