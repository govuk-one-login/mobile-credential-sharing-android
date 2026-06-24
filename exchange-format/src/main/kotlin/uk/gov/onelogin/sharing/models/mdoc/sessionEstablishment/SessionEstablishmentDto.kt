package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborErrors
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG

/**
 * ```
 * SessionEstablishment = {
 * "eReaderKey" : EReaderKeyBytes,
 * "data" : bstr ; Encrypted mdoc request
 * * tstr => RFU
 * }
 * ```
 */
@JsonSerialize(using = SessionEstablishmentDto.Serializer::class)
@JsonDeserialize(using = SessionEstablishmentDto.Deserializer::class)
data class SessionEstablishmentDto(val eReaderKey: EmbeddedCbor, val data: ByteArray) :
    CborEncodable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionEstablishmentDto

        if (eReaderKey != other.eReaderKey) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eReaderKey.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    class Serializer : StdSerializer<SessionEstablishmentDto>(SessionEstablishmentDto::class.java) {
        override fun serialize(
            value: SessionEstablishmentDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldName(E_READER_KEY_KEY)
            gen.writeTag(EMBEDDED_CBOR_TAG)
            gen.writeBinary(value.eReaderKey.encoded)
            gen.writeFieldName(DATA_KEY)
            gen.writeBinary(value.data)
            gen.writeEndObject()
        }
    }

    class Deserializer : JsonDeserializer<SessionEstablishmentDto>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): SessionEstablishmentDto {
            val root = p.codec.readTree<JsonNode>(p)

            require(root.isObject) {
                CborErrors.DECODING_ERROR.errorMessage
            }

            val eReaderKeyNode = root[E_READER_KEY_KEY]
            requireNotNull(eReaderKeyNode) {
                CborErrors.PARSING_ERROR.errorMessage
            }

            val dataNode = root[DATA_KEY]
            requireNotNull(dataNode) {
                CborErrors.PARSING_ERROR.errorMessage
            }

            val eReaderKeyBytes = eReaderKeyNode.binaryValue()
            val dataBytes = dataNode.binaryValue()

            return SessionEstablishmentDto(
                eReaderKey = EmbeddedCbor(eReaderKeyBytes),
                data = dataBytes
            )
        }
    }

    companion object {
        private const val FIELD_COUNT = 2
        const val E_READER_KEY_KEY = "eReaderKey"
        const val DATA_KEY = "data"
    }
}
