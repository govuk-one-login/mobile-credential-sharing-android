package uk.gov.onelogin.sharing.security.cbor.dto.devicerequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class ItemsRequestDto(
    @JsonProperty("docType")
    val docType: String,
    @JsonProperty("nameSpaces")
    val nameSpaces: Map<String, Map<String, Boolean>>,
    @JsonIgnore
    val requestInfo: ByteArray? = null
)
