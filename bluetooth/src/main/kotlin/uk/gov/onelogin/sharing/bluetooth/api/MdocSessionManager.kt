package uk.gov.onelogin.sharing.bluetooth.api

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStatus

/**
 * Responsible for orchestrating the BLE advertising and GATT service
 *
 * [MdocSessionState] via a [StateFlow].
 */
interface MdocSessionManager {
    /**
     * The current state of the BLE session, exposed as a [StateFlow].
     * This can be used to observe the session's status, such as whether it's advertising,
     * connected, or has encountered an error.
     */
    val state: StateFlow<MdocSessionState>

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
     */
    suspend fun stop()
}
