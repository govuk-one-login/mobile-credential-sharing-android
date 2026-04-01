package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor

class DeviceResponseDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class DeviceResponse(
        @JsonProperty("version")
        val version: String = "1.0",

        @JsonProperty("documents")
        val documents: List<DocumentDTO>?,

        @JsonProperty("documentErrors")
        val documentErrors: Map<String, Int>?,

        @JsonProperty("status")
        val status: Int
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class DocumentDTO(
        @JsonProperty("docType")
        val docType: String,

        @JsonProperty("issuerSigned")
        val issuerSigned: EmbeddedCbor,

        @JsonProperty("deviceSigned")
        val deviceSigned: EmbeddedCbor,

        @JsonProperty("errors")
        val errors: Map<String, Int>? = null
    )

}