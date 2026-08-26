
package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import java.io.OutputStream
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.RawCbor
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

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
    @JsonSerialize(using = DeviceResponseSerializer::class)
    @JsonDeserialize(using = DeviceResponseDeserializer::class)
    data class DeviceResponseDTO(
        val version: String = "1.0",
        val documents: List<DocumentDTO>? = null,
        val documentErrors: Map<String, UInt>? = null,
        val status: UInt,
        @JsonIgnore
        val rawBytes: ByteArray? = null
    ) : CborEncodable {
        init {
            require(version.startsWith("1.")) {
                "Received invalid device response version: $version"
            }
            require(status in Status.applicableCodes) {
                "Received invalid device response status code: $status"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DeviceResponseDTO

            if (version != other.version) return false
            if (documents != other.documents) return false
            if (documentErrors != other.documentErrors) return false
            if (status != other.status) return false
            if (!rawBytes.contentEquals(other.rawBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = version.hashCode()
            result = 31 * result + (documents?.hashCode() ?: 0)
            result = 31 * result + (documentErrors?.hashCode() ?: 0)
            result = 31 * result + status.hashCode()
            result = 31 * result + (rawBytes?.contentHashCode() ?: 0)
            return result
        }

        /**
         * Maps to domain preserving original Tag 24-encoded bytes.
         *
         * Requires [rawBytes] to be set with the original CBOR bytes from which
         * this DTO was deserialized.
         */
        fun toDomain(): DeviceResponse {
            val sourceBytes = requireNotNull(rawBytes) {
                RAW_BYTES_MISSING_ERROR
            }
            val extractedBytes = DeviceResponseCborExtractor.extract(sourceBytes)
            check(extractedBytes.size == (documents?.size ?: 0)) {
                "Extractor document count (${extractedBytes.size}) != " +
                    "DTO document count (${documents?.size ?: 0})"
            }
            return DeviceResponse(
                statusCode = status,
                documents = documents?.mapIndexed { index, doc ->
                    val raw = extractedBytes[index]
                    doc.copy(
                        issuerSigned = doc.issuerSigned.copy(rawBytes = raw.issuerSigned),
                        deviceSigned = doc.deviceSigned.copy(rawBytes = raw.deviceSigned)
                    ).toDomain()
                },
                version = version
            )
        }

        companion object {
            /**
             * Decodes a CBOR byte array into a [DeviceResponseDTO] with [rawBytes] populated.
             */
            fun decode(bytes: ByteArray): DeviceResponseDTO = CborMapper.default.readValue(
                bytes,
                DeviceResponseDTO::class.java
            ).copy(rawBytes = bytes)
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
    data class DocumentDTO(
        val docType: String,
        val issuerSigned: IssuerSignedDTO,
        val deviceSigned: DeviceSignedDTO,
        val errors: Map<String, Int>? = null
    ) {
        fun toDomain(): VerifiableDocument.WithPresentation =
            SharingVerifiableDocumentWithPresentation(
                docType = docType,
                issuerSigned = issuerSigned.toDomain(),
                deviceSigned = deviceSigned.toDomain()
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
        val nameSpaces: Map<String, List<EmbeddedCbor>>? = null,
        val issuerAuth: RawCbor,
        @JsonIgnore
        val rawBytes: IssuerSignedRawBytes? = null
    ) {
        fun toDomain(): SharingIssuerSigned {
            val raw = requireNotNull(rawBytes) {
                RAW_BYTES_MISSING_ERROR
            }
            return SharingIssuerSigned(
                nameSpaces = raw.nameSpaces.ifEmpty { null },
                issuerAuth = raw.issuerAuthBytes ?: issuerAuth.encoded
            )
        }
    }

    @JsonDeserialize(using = IssuerSignedItemDeserializer::class)
    data class IssuerSignedItemDTO(
        val digestId: Long,
        val random: ByteArray,
        val elementIdentifier: String,
        val elementValue: Any
    )

    class IssuerSignedItemDeserializer :
        StdDeserializer<IssuerSignedItemDTO>(IssuerSignedItemDTO::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IssuerSignedItemDTO {
            val root = p.codec.readTree<JsonNode>(p)
            return IssuerSignedItemDTO(
                digestId = root["digestID"].longValue(),
                random = root["random"].binaryValue(),
                elementIdentifier = root["elementIdentifier"].asText(),
                elementValue = root["elementValue"].let { node ->
                    when {
                        node.isTextual -> node.asText()
                        node.isBoolean -> node.booleanValue()
                        node.isNumber -> node.numberValue()
                        else -> node.toString()
                    }
                }
            )
        }
    }

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
        val nameSpaces: EmbeddedCbor,
        val deviceAuth: DeviceAuthDTO,
        @JsonIgnore
        val rawBytes: DeviceSignedRawBytes? = null
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

        fun toDomain(): SharingDeviceSigned {
            val raw = requireNotNull(rawBytes) {
                RAW_BYTES_MISSING_ERROR
            }
            return SharingDeviceSigned(
                deviceNameSpacesBytes = raw.nameSpacesBytes ?: nameSpaces.encoded,
                deviceSignature = raw.signatureBytes ?: deviceAuth.deviceSignature.encoded
            )
        }
    }

    data class DeviceAuthDTO(val deviceSignature: RawCbor)

    class DeviceResponseSerializer :
        StdSerializer<DeviceResponseDTO>(DeviceResponseDTO::class.java) {

        override fun serialize(
            value: DeviceResponseDTO,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            val cborGen = gen as CBORGenerator
            val fieldCount = DEVICE_RESPONSE_REQUIRED_FIELDS +
                (if (value.documents != null) 1 else 0) +
                (if (value.documentErrors != null) 1 else 0)
            cborGen.writeStartObject(fieldCount)
            cborGen.writeFieldName(KEY_VERSION)
            cborGen.writeString(value.version)
            if (value.documents != null) {
                cborGen.writeFieldName(KEY_DOCUMENTS)
                cborGen.writeStartArray(value.documents, value.documents.size)
                value.documents.forEach { doc -> serializeDocument(doc, cborGen) }
                cborGen.writeEndArray()
            }
            if (value.documentErrors != null) {
                cborGen.writeFieldName(KEY_DOCUMENT_ERRORS)
                cborGen.writeStartObject(value.documentErrors.size)
                value.documentErrors.forEach { (k, v) ->
                    cborGen.writeFieldName(k)
                    cborGen.writeNumber(v.toInt())
                }
                cborGen.writeEndObject()
            }
            cborGen.writeFieldName(KEY_STATUS)
            cborGen.writeNumber(value.status.toInt())
            cborGen.writeEndObject()
        }

        private fun serializeDocument(doc: DocumentDTO, gen: CBORGenerator) {
            val fieldCount = DOCUMENT_REQUIRED_FIELDS + (if (doc.errors != null) 1 else 0)
            gen.writeStartObject(fieldCount)
            gen.writeFieldName(KEY_DOC_TYPE)
            gen.writeString(doc.docType)
            gen.writeFieldName(KEY_ISSUER_SIGNED)
            serializeIssuerSigned(doc.issuerSigned, gen)
            gen.writeFieldName(KEY_DEVICE_SIGNED)
            serializeDeviceSigned(doc.deviceSigned, gen)
            if (doc.errors != null) {
                gen.writeFieldName("errors")
                gen.writeStartObject(doc.errors.size)
                doc.errors.forEach { (k, v) ->
                    gen.writeFieldName(k)
                    gen.writeNumber(v)
                }
                gen.writeEndObject()
            }
            gen.writeEndObject()
        }

        private fun serializeIssuerSigned(issuerSigned: IssuerSignedDTO, gen: CBORGenerator) {
            gen.writeStartObject()
            if (issuerSigned.nameSpaces != null) {
                gen.writeFieldName(KEY_NAME_SPACES)
                gen.writeStartObject(issuerSigned.nameSpaces.size)
                issuerSigned.nameSpaces.forEach { (ns, items) ->
                    gen.writeFieldName(ns)
                    gen.writeStartArray(items, items.size)
                    items.forEach { item ->
                        gen.writeTag(EMBEDDED_CBOR_TAG)
                        gen.writeBinary(item.encoded)
                    }
                    gen.writeEndArray()
                }
                gen.writeEndObject()
            }
            gen.writeFieldName(KEY_ISSUER_AUTH)
            gen.flush()
            (gen.outputTarget as OutputStream).write(issuerSigned.issuerAuth.encoded)
            gen.writeEndObject()
        }

        private fun serializeDeviceSigned(deviceSigned: DeviceSignedDTO, gen: CBORGenerator) {
            gen.writeStartObject()
            gen.writeFieldName(KEY_NAME_SPACES)
            gen.writeTag(EMBEDDED_CBOR_TAG)
            gen.writeBinary(deviceSigned.nameSpaces.encoded)
            gen.writeFieldName(KEY_DEVICE_AUTH)
            gen.writeStartObject()
            gen.writeFieldName(KEY_DEVICE_SIGNATURE)
            gen.flush()
            (gen.outputTarget as OutputStream).write(
                deviceSigned.deviceAuth.deviceSignature.encoded
            )
            gen.writeEndObject()
            gen.writeEndObject()
        }

        private companion object {
            const val DEVICE_RESPONSE_REQUIRED_FIELDS = 2 // version + status
            const val DOCUMENT_REQUIRED_FIELDS = 3 // docType + issuerSigned + deviceSigned
        }
    }

    /**
     * Deserializes a CBOR byte array into [DeviceResponse].
     *
     * Preserves Tag 24 items within `issuerSigned.nameSpaces` as raw byte arrays.
     *
     * The `issuerAuth` and `deviceSignature` fields are re-encoded from the parsed tree
     * for DTO use. During `toDomain()`, these are replaced by exact original bytes from
     * [DeviceResponseCborExtractor].
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
            val docType = docNode[KEY_DOC_TYPE]?.asText()
                ?: throw IllegalArgumentException("Missing 'docType' in Document")
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
    }

    internal companion object {
        const val KEY_VERSION = "version"
        const val KEY_STATUS = "status"
        const val KEY_DOCUMENTS = "documents"
        const val KEY_DOCUMENT_ERRORS = "documentErrors"
        const val KEY_DOC_TYPE = "docType"
        const val KEY_ISSUER_SIGNED = "issuerSigned"
        const val KEY_DEVICE_SIGNED = "deviceSigned"
        const val KEY_NAME_SPACES = "nameSpaces"
        const val KEY_ISSUER_AUTH = "issuerAuth"
        const val KEY_DEVICE_AUTH = "deviceAuth"
        const val KEY_DEVICE_SIGNATURE = "deviceSignature"

        const val RAW_BYTES_MISSING_ERROR = "rawBytes must be set before calling toDomain()"
    }
}
