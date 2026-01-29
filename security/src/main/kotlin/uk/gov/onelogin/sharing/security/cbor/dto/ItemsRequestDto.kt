package uk.gov.onelogin.sharing.security.cbor.dto

data class ItemsRequestDto(
    val docType: String,
    val nameSpaces: Map<String, Map<String, Boolean>>
)
