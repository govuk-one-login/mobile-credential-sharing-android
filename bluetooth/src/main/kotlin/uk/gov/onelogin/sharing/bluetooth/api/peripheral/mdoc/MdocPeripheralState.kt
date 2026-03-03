package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

/**
 * Represents the combined states from advertising and the GATT service.
 */
sealed interface MdocPeripheralState {
    /** This is the initial state. */
    data object Idle : MdocPeripheralState

    /** BLE advertising is in progress. */
    data object AdvertisingStarted : MdocPeripheralState

    /** BLE advertising has stopped. */
    data object AdvertisingStopped : MdocPeripheralState

    /** GATT service stopped */
    data object GattServiceStopped : MdocPeripheralState

    /**
     * The device has successfully connected.
     *
     * @param address The address of the connected device.
     */
    data class Connected(val address: String) : MdocPeripheralState

    /**
     * The GATT service has been successfully added.
     *
     * @param uuid The uuid of the service.
     */
    data class ServiceAdded(val uuid: UUID?) : MdocPeripheralState

    /**
     * The device has disconnected.
     *
     * @param address The address of the disconnected device, which may be null if the address
     * is not known.
     */
    data class Disconnected(val address: String?, val isSessionEnd: Boolean) : MdocPeripheralState

    /**
     * An error occurred during the session. This can be and error
     * from the Advertiser or the GATT service
     *
     * @param reason The [MdocPeripheralTransportError] that occurred.
     */
    data class Error(val reason: MdocPeripheralTransportError) : MdocPeripheralState

    /**
     * A session end command has been received from the client or server manager
     *
     */
    data class MdocPeripheralEnded(val status: SessionEndStates) : MdocPeripheralState

    data class MessageReceived(val message: ByteArray) : MdocPeripheralState
}
