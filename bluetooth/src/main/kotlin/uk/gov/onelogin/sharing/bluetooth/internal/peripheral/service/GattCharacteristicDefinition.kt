package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service

import java.util.UUID

data class GattCharacteristicDefinition(
    val uuid: UUID,
    val properties: Int,
    val permissions: Int,
    // Client Characteristic Configuration Descriptor
    val hasCccd: Boolean = false
)
