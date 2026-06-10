package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.api.core.MessageSender

/**
 * Responsible for orchestrating the BLE advertising and GATT service
 *
 * [PeripheralBluetoothState] via a [StateFlow].
 */
interface PeripheralBluetoothTransport : MessageSender,
    BluetoothTransport<PeripheralBluetoothState> {
    /**
     * Stops the BLE advertising and GATT service.
     *
     * @param serviceUuid The [UUID] of the service to send the end command
     * @param sendEndCommand Used trigger the state end (0x02) command.
     * If the peripheral tiggers the disconnection, it should send the end command
     * before the teardown
     * If the disconnection is triggered from the other side, it shouldn't send the end command
     */
    suspend fun stop(serviceUuid: UUID, sendEndCommand: Boolean)

    /**
     * Notifies the client to end the session with end code 0x02
     */
    suspend fun notifySessionEnd(serviceUuid: UUID)
}
