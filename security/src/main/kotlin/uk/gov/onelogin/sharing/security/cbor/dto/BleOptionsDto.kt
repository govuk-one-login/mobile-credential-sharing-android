package uk.gov.onelogin.sharing.security.cbor.dto

data class BleOptionsDto(
    val serverMode: Boolean,
    val clientMode: Boolean,
    val peripheralServerModeUuid: ByteArray?
)
