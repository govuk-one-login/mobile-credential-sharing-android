package uk.gov.onelogin.sharing.security.cbor.dto.devicerequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class DeviceRequestDto(
    @JsonProperty("version")
    val version: String,
    @JsonProperty("docRequests")
    val docRequest: List<DocRequestDto>,
    @JsonIgnore
    val deviceRequestInfo: ByteArray? = null,
    @JsonIgnore
    val readerAuthAll: ByteArray? = null
)
