package uk.gov.onelogin.sharing.security.cbor.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uk.gov.onelogin.sharing.security.cbor.deserializers.DeviceRetrievalMethodsDeserializer
import uk.gov.onelogin.sharing.security.cbor.deserializers.SecurityDeserializer

data class DeviceEngagementDto(
    @JsonProperty("0")
    val version: String,
    @JsonProperty("1")
    @JsonDeserialize(using = SecurityDeserializer::class)
    val security: SecurityDto,
    @JsonProperty("2")
    @JsonDeserialize(using = DeviceRetrievalMethodsDeserializer::class)
    val deviceRetrievalMethods: List<DeviceRetrievalMethodDto>
)
