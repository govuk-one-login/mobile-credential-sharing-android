package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import java.util.UUID
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID

data class DeviceRetrievalMethodDto(val type: Int, val version: Int, val options: BleOptionsDto) {
    fun getPeripheralServerModeUuidString(): String? = options.getPeripheralServerModeUuidString()
    fun getPeripheralServerModeUuid(): UUID? = options.peripheralServerModeUuid?.toUUID()
}
