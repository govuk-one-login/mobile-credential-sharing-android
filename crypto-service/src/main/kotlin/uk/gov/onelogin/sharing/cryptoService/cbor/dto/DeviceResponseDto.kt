package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.RawCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
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
    @JsonDeserialize(using = DeviceResponseDeserializer::class)
    data class DeviceResponseDTO(
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

        fun toDomain(): DeviceResponse = DeviceResponse(
            statusCode = status,
            documents = documents?.map { it.toDomain() },
            version = version
        )
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
    ) {
        fun toDomain(): Document = Document(
            docType = docType,
            issuerSigned = issuerSigned.toDomain(),
            deviceSigned = DeviceSigned(
                nameSpaces = deviceSigned.nameSpaces.encoded,
                deviceAuth = deviceSigned.deviceAuth.deviceSignature.encoded
            )
        )
    }

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
    ) {
        fun toDomain(): IssuerSigned = IssuerSigned(
            nameSpaces = nameSpaces?.mapValues { entry ->
                entry.value.map { it.encoded }
            },
            issuerAuth = issuerAuth.encoded
        )
    }

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
    ) {
        init {
            val nameSpacesMap = CborMapper.default.readValue(
                nameSpaces.encoded,
                Map::class.java
            )

            require(nameSpacesMap.isEmpty()) {
                "Received unexpected data in 'nameSpaces' property: $nameSpacesMap"
            }
        }
    }

    data class DeviceAuthDTO(
        @JsonProperty("deviceSignature")
        val deviceSignature: RawCbor
    )

    /**
     * Deserializes a CBOR byte array into [DeviceResponse].
     *
     * Preserves Tag 24 items within `issuerSigned.nameSpaces` as raw byte arrays
     *
     * The `issuerAuth` and `deviceSignature` fields are re-encoded from the parsed tree
     * to capture the raw COSE structure.
     */
    class DeviceResponseDeserializer :
        StdDeserializer<DeviceResponseDTO>(DeviceResponseDTO::class.java) {

        override fun deserialize(
            jsonParser: JsonParser,
            context: DeserializationContext
        ): DeviceResponseDTO {
            val root = CborMapper.default.readTree<JsonNode>(jsonParser)

            val version = root[KEY_VERSION]?.asText()
                ?: throw IllegalArgumentException("Missing 'version' in DeviceResponse")
            val status = root[KEY_STATUS]?.asInt()?.toUInt()
                ?: throw IllegalArgumentException("Missing 'status' in DeviceResponse")

            val documents = if (root.has(KEY_DOCUMENTS)) {
                root[KEY_DOCUMENTS].map { docNode ->
                    deserializeDocument(docNode)
                }
            } else {
                null
            }

            return DeviceResponseDTO(
                version = version,
                documents = documents,
                status = status
            )
        }

        private fun deserializeDocument(docNode: JsonNode): DocumentDTO {
            val docType = docNode[KEY_DOC_TYPE].asText()
            val issuerSigned = deserializeIssuerSigned(docNode[KEY_ISSUER_SIGNED])
            val deviceSigned = deserializeDeviceSigned(docNode[KEY_DEVICE_SIGNED])

            return DocumentDTO(
                docType = docType,
                issuerSigned = issuerSigned,
                deviceSigned = deviceSigned
            )
        }

        private fun deserializeIssuerSigned(node: JsonNode): IssuerSignedDTO {
            val nameSpaces = if (node.has(KEY_NAME_SPACES)) {
                val nsNode = node[KEY_NAME_SPACES]
                val result = mutableMapOf<String, List<EmbeddedCbor>>()
                nsNode.fieldNames().forEach { nameSpace ->
                    val items = nsNode[nameSpace].map { itemNode ->
                        EmbeddedCbor(itemNode.binaryValue())
                    }
                    result[nameSpace] = items
                }
                result
            } else {
                null
            }

            val issuerAuthBytes = CborMapper.default.writeValueAsBytes(
                node[KEY_ISSUER_AUTH]
            )

            return IssuerSignedDTO(
                nameSpaces = nameSpaces,
                issuerAuth = RawCbor(issuerAuthBytes)
            )
        }

        private fun deserializeDeviceSigned(node: JsonNode): DeviceSignedDTO {
            val nameSpacesBytes = node[KEY_NAME_SPACES].binaryValue()
            val deviceSignatureBytes = CborMapper.default.writeValueAsBytes(
                node[KEY_DEVICE_AUTH][KEY_DEVICE_SIGNATURE]
            )

            return DeviceSignedDTO(
                nameSpaces = EmbeddedCbor(nameSpacesBytes),
                deviceAuth = DeviceAuthDTO(
                    deviceSignature = RawCbor(deviceSignatureBytes)
                )
            )
        }

        private companion object {
            const val KEY_VERSION = "version"
            const val KEY_STATUS = "status"
            const val KEY_DOCUMENTS = "documents"
            const val KEY_DOC_TYPE = "docType"
            const val KEY_ISSUER_SIGNED = "issuerSigned"
            const val KEY_DEVICE_SIGNED = "deviceSigned"
            const val KEY_NAME_SPACES = "nameSpaces"
            const val KEY_ISSUER_AUTH = "issuerAuth"
            const val KEY_DEVICE_AUTH = "deviceAuth"
            const val KEY_DEVICE_SIGNATURE = "deviceSignature"
        }
    }
}
