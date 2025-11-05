package uk.gov.onelogin.sharing.bluetooth.advertiser

import android.Manifest
import android.bluetooth.le.AdvertisingSetParameters
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
     * @param payload The payload to advertise.
     * @param parameters The advertising parameters.
     *
     * @return [AdvertiserStartResult] The result of the start advertising operation.
     * Monitor [state] to confirm the service has successfully started advertising.
     */
    suspend fun startAdvertise(
        payload: Payload,
        parameters: AdvertisingSetParameters,
    ): AdvertiserStartResult

    /**
     * Stop advertising the BLE service.
     */
    suspend fun stopAdvertise()

    /**
     * Check if the bluetooth is enabled.
     */
    fun isBluetoothEnabled(): Boolean

    /**
     * Check if the bluetooth adapter has the [Manifest.permission.BLUETOOTH_ADVERTISE] permission.
     */
    fun hasAdvertisePermission(): Boolean


    val state: StateFlow<AdvertiserState>

    interface Payload {
        fun asBytes(): ByteArray
    }
}