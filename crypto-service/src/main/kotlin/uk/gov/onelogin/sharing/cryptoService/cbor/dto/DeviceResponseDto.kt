package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.RawCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

class DeviceResponseDto {

    /**
     * ```
     * DeviceResponse = {
     *   "version" : tstr,
     *   ? "documents" : [+ Document],
     *   "status" : uint
     * }
     * ```
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class DeviceResponse(
        @JsonProperty("version")
        val version: String = "1.0",

        @JsonProperty("documents")
        val documents: List<DocumentDTO>? = null,

        @JsonProperty("documentErrors")
        val documentErrors: Map<String, UInt>? = null,

        @JsonProperty("status")
        val status: UInt
    ) {
        init {
            require(version.startsWith("1.")) {
                "Received invalid device response version: $version"
            }
            require(status in Status.applicableCodes) {
                "Received invalid device response status code: $status"
            }
        }
    }

    /**
     * ```
     * Document = {
     *   "docType" : DocType (tstr),
     *   "issuerSigned" : IssuerSigned,
     *   "deviceSigned" : DeviceSigned
     * }
     * ```
     */
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

    /**
     * ```
     * IssuerSigned = {
     *   "issuerAuth" : IssuerAuth,
     *   ? "nameSpaces" : IssuerNameSpaces
     * }
     * ```
     */
    data class IssuerSignedDTO(
        @JsonProperty("nameSpaces")
        val nameSpaces: Map<String, List<EmbeddedCbor>>? = null,

        @JsonProperty("issuerAuth")
        val issuerAuth: RawCbor
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

    /**
     * ```
     * DeviceSigned = {
     *   "nameSpaces" : DeviceNameSpacesBytes,
     *   "deviceAuth" : DeviceAuth
     * }
     *
     * DeviceNameSpacesBytes = #6.24(bstr .cbor DeviceNameSpaces)
     * ```
     */
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
