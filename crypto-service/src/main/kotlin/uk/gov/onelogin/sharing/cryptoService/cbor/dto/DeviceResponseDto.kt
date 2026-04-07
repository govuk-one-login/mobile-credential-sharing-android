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
        val issuerSigned: IssuerSignedDTO,

        @JsonProperty("deviceSigned")
        val deviceSigned: DeviceSignedDTO,

        @JsonProperty("errors")
        val errors: Map<String, Int>? = null
    )

    data class IssuerSignedDTO(
        @JsonProperty("nameSpaces")
        val nameSpaces: Map<String, List<EmbeddedCbor>>?,

        @JsonProperty("issuerAuth")
        val issuerAuth: ByteArray
    )

    data class IssuerSignedItemDTO(
        @JsonProperty("digestID")
        val digestId: Long,

        @JsonProperty("random")
        val random: ByteArray,

        @JsonProperty("elementIdentifier")
        val elementIdentifier: String,

        @JsonProperty("elementValue")
        val elementValue: Any
    )

    data class DeviceSignedDTO(
        @JsonProperty("nameSpaces")
        val nameSpaces: EmbeddedCbor,

        @JsonProperty("deviceAuth")
        val deviceAuth: DeviceAuthDTO
    )

    data class DeviceAuthDTO(
        @JsonProperty("deviceSignature")
        val deviceSignature: ByteArray
    )
}
