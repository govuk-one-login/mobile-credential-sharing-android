package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothDevice
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.central.GattClientManager
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

class FakeGattClientManager : GattClientManager {
    private val _events = MutableSharedFlow<GattClientEvent>()
    override val events: SharedFlow<GattClientEvent> = _events

    var connectCalls = 0
    var disconnectCalls = 0
    var sendMessageToReturn: Boolean = true
    var lastSentData: ByteArray? = null

    var notifySessionEndCalls = 0

    override fun connect(device: BluetoothDevice, serviceUuid: UUID) {
        connectCalls++
    }

    override fun disconnect() {
        disconnectCalls++
    }

    override fun notifySessionEnd(): SessionEndStates {
        notifySessionEndCalls++
        return SessionEndStates.SUCCESS
    }

    override suspend fun sendMessage(serviceUuid: UUID, data: ByteArray): Boolean {
        lastSentData = data
        return sendMessageToReturn
    }

    suspend fun emitEvent(event: GattClientEvent) {
        _events.emit(event)
    }
}
