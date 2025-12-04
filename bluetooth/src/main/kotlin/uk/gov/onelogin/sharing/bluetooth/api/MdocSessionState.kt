package uk.gov.onelogin.sharing.bluetooth.api

import java.util.UUID

/**
 * Represents the combined states from advertising and the GATT service.
 */
sealed interface MdocSessionState {
    /** This is the initial state. */
    data object Idle : MdocSessionState

    /** BLE advertising is in progress. */
    data object AdvertisingStarted : MdocSessionState

    /** BLE advertising has stopped. */
    data object AdvertisingStopped : MdocSessionState

    /** GATT service stopped */
    data object GattServiceStopped : MdocSessionState

    /**
     * The device has successfully connected.
     *
     * @param address The address of the connected device.
     */
    data class Connected(val address: String) : MdocSessionState

    /**
     * The GATT service has been successfully added.
     *
     * @param uuid The uuid of the service.
     */
    data class ServiceAdded(val uuid: UUID?) : MdocSessionState

    /**
     * The device has disconnected.
     *
     * @param address The address of the disconnected device, which may be null if the address
     * is not known.
     */
    data class Disconnected(val address: String?) : MdocSessionState

    /**
     * An error occurred during the session. This can be and error
     * from the Advertiser or the GATT service
     *
     * @param reason The [MdocSessionError] that occurred.
     */
    data class Error(val reason: MdocSessionError) : MdocSessionState
}
