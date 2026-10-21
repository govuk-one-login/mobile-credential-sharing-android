package uk.gov.onelogin.sharing.bluetooth.api.central

import android.bluetooth.BluetoothDevice
import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.MessageSender
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

interface GattClientManager : MessageSender {
    val events: SharedFlow<GattClientEvent>

    fun connect(device: BluetoothDevice, serviceUuid: UUID)

    fun disconnect()

    fun notifySessionEnd(): SessionEndStates
}
