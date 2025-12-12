package uk.gov.onelogin.sharing.core.mdoc

import java.util.UUID

/**
 * UUID definitions for the mdoc BLE GATT service as specified in
 * ISO/IEC 18013-5:2021(E), Section 11.1.3.2 Service definition - Table 5
 *
 */
object GattUuids {
    /**
     * The UUID for the Client Characteristic Configuration Descriptor (CCCD).
     *
     * As required by ISO/IEC 18013-5, any characteristic that supports Notify property must
     * include this descriptor with UUID `0x29 0x02` and default value of `0x00 0x00`.
     *
     */
    val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    /**
     * UUID for the State characteristic.
     */
    val STATE_UUID: UUID = UUID.fromString("00000001-A123-48CE-896B-4C76973373E6")

    /**
     * UUID for the Client2Server characteristic.
     */
    val CLIENT_2_SERVER_UUID: UUID = UUID.fromString("00000002-A123-48CE-896B-4C76973373E6")

    /**
     * UUID for the Server2Client characteristic.
     */
    val SERVER_2_CLIENT_UUID: UUID = UUID.fromString("00000003-A123-48CE-896B-4C76973373E6")
}
