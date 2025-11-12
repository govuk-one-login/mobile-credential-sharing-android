package uk.gov.onelogin.sharing.bluetooth.api

import kotlinx.coroutines.flow.StateFlow

/**
 * Handles advertising BLE service
 *
 * This class is responsible for managing the BLE service and tracking the state
 */

interface BleAdvertiser {
    /**
     * Start advertising with the given payload and parameters.
     *
     * @param bleAdvertiseData The payload to advertise.
     *
     * @return [AdvertiserStartResult] The result of the start advertising operation.
     * Monitor [state] to confirm the service has successfully started advertising.
     */
    suspend fun startAdvertise(bleAdvertiseData: BleAdvertiseData): AdvertiserStartResult

    /**
     * Stop advertising the BLE service.
     */
    suspend fun stopAdvertise()

    /**
     * Check if the bluetooth is enabled.
     */
    fun isBluetoothEnabled(): Boolean

    /**
     * Check if the bluetooth adapter has the
     * [android.Manifest.permission.BLUETOOTH_ADVERTISE] permission.
     */
    fun hasAdvertisePermission(): Boolean

    val state: StateFlow<AdvertiserState>

    fun interface Payload {
        fun asBytes(): ByteArray
    }
}
