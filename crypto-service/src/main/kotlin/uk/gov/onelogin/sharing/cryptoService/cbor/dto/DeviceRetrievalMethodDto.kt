package uk.gov.onelogin.sharing.cryptoService.cbor.dto

data class DeviceRetrievalMethodDto(val type: Int, val version: Int, val options: BleOptionsDto) {
    fun getPeripheralServerModeUuidString(): String? = options.getPeripheralServerModeUuidString()
    fun getPeripheralServerModeUuid(): ByteArray? = options.peripheralServerModeUuid
}
