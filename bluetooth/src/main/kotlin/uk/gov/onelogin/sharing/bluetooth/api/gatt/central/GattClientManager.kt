package uk.gov.onelogin.sharing.bluetooth.api.gatt.central

import android.bluetooth.BluetoothDevice
import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.MessageSender
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

interface GattClientManager : MessageSender {
    val events: SharedFlow<GattClientEvent>

    fun connect(device: BluetoothDevice, serviceUuid: UUID)

    fun disconnect()

    suspend fun notifySessionEnd(disconnect: Boolean = false): SessionEndStates
}
