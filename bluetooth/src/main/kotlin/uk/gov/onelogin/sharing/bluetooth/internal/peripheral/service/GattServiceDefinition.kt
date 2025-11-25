package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service

import java.util.UUID

data class GattServiceDefinition(
    val uuid: UUID,
    val characteristics: List<GattCharacteristicDefinition>
)
