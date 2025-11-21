package uk.gov.onelogin.sharing.security.cbor.dto

data class DeviceRetrievalMethodDto(val type: Int, val version: Int, val options: BleOptionsDto)
