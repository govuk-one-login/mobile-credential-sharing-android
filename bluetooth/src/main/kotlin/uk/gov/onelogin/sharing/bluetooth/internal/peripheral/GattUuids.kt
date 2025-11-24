package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import java.util.UUID

internal object GattUuids {
    val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    val MDOC_SERVICE_ID: UUID = UUID.fromString("00000000-A123-48CE-896B-4C76973373E6")

    val STATE_UUID: UUID = UUID.fromString("00000001-A123-48CE-896B-4C76973373E6")

    val CLIENT_2_SERVER_UUID: UUID = UUID.fromString("00000002-A123-48CE-896B-4C76973373E6")

    val SERVER_2_CLIENT_UUID: UUID = UUID.fromString("00000003-A123-48CE-896B-4C76973373E6")
}
