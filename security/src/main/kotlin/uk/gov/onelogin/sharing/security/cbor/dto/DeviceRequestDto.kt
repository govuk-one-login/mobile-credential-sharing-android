package uk.gov.onelogin.sharing.security.cbor.dto

data class DeviceRequestDto(
    val version: String,
    val docRequests : List<ItemsRequestDto>
)