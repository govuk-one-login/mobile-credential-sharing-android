package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service

import java.util.UUID

/**
 * Defines properties of a GATT characteristic used to create and configure the GATT service.
 *
 * @param uuid The unique identifier for the characteristic.
 * @param properties The properties of the characteristic, such as read, write, or notify.
 * @param permissions The permissions for reading and writing the characteristic's value.
 * @param hasCccd Whether the characteristic should include a Client Characteristic Configuration
 * Descriptor (CCCD). This is necessary for characteristics that support
 * notifications. Defaults to `false`.
 */
data class GattCharacteristicDefinition(
    val uuid: UUID,
    val properties: Int,
    val permissions: Int,
    val hasCccd: Boolean = false
)
