package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.MessageSender

/**
 * Responsible for orchestrating the BLE advertising and GATT service
 *
 * [PeripheralBluetoothState] via a [StateFlow].
 */
interface PeripheralBluetoothTransport : MessageSender {
    /**
     * The current state of the BLE session, exposed as a [StateFlow].
     * This can be used to observe the session's status, such as whether it's advertising,
     * connected, or has encountered an error.
     */
    val state: StateFlow<PeripheralBluetoothState>

    /**
     * Current state of the Bluetooth adapter, exposed as a [StateFlow].
     *
     * This exposes the broadcast receiver that listens for Bluetooth state changes.
     */
    val bluetoothStatus: StateFlow<BluetoothStatus>

    /**
     * Starts the BLE advertising and GATT service.
     *
     * @param serviceUuid The [UUID] of the service to be advertised.
     */
    suspend fun start(serviceUuid: UUID)

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
