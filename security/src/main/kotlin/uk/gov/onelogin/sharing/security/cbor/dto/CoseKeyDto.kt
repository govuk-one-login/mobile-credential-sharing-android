package uk.gov.onelogin.sharing.security.cbor.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CoseKeyDto(
    @JsonProperty("1")
    val keyType: Long,
    @JsonProperty("-1")
    val curve: Long,
    @JsonProperty("-2")
    val x: ByteArray,
    @JsonProperty("-3")
    val y: ByteArray
)
