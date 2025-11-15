package uk.gov.onelogin.sharing.bluetooth.api

import kotlinx.coroutines.flow.StateFlow

/**
 * An interface for a Bluetooth Low Energy (BLE) advertiser.
 *
 * The state of the advertiser can be observed via [state].
 * The start advertise result will indicate if the start advertising operation was successful.
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

    /**
     * The current state of the advertiser, exposed as a [StateFlow].
     * This can be used to observe the advertiser's status, such as whether it's advertising,
     * idle, or has encountered an error after initialization
     */
    val state: StateFlow<AdvertiserState>
}
