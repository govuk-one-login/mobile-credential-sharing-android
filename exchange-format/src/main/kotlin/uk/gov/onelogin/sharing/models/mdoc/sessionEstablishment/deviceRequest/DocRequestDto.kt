package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

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
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG

/**
 * ```
 * DocRequest = {
 *     "itemsRequest" : ItemsRequestBytes,
 *     ? "readerAuth" : ReaderAuth,
 *     * tstr => RFU
 * }
 * ```
 */
@JsonSerialize(using = DocRequestDto.Serializer::class)
@JsonDeserialize(using = DocRequestDto.Deserializer::class)
data class DocRequestDto(
    val itemsRequest: ItemsRequestDto,
    @JsonIgnore
    val readerAuth: ByteArray? = null
) : CborEncodable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocRequestDto

        if (itemsRequest != other.itemsRequest) return false
        if (!readerAuth.contentEquals(other.readerAuth)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = itemsRequest.hashCode()
        result = 31 * result + (readerAuth?.contentHashCode() ?: 0)
        return result
    }

    class Serializer : StdSerializer<DocRequestDto>(DocRequestDto::class.java) {
        override fun serialize(
            value: DocRequestDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldName(ITEMS_REQUEST_KEY)
            gen.writeTag(EMBEDDED_CBOR_TAG)
            gen.writeBinary(CborMapper.default.writeValueAsBytes(value.itemsRequest))
            gen.writeEndObject()
        }
    }

    class Deserializer : StdDeserializer<DocRequestDto>(DocRequestDto::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DocRequestDto {
            val root = p.codec.readTree<JsonNode>(p)
            val itemsRequestNode = root[ITEMS_REQUEST_KEY]
                ?: throw IllegalArgumentException("Missing itemsRequest in DocRequest")
            val itemsRequest = CborMapper.default
                .readValue(itemsRequestNode.binaryValue(), ItemsRequestDto::class.java)
            return DocRequestDto(itemsRequest = itemsRequest)
        }
    }

    companion object {
        private const val FIELD_COUNT = 1
        const val ITEMS_REQUEST_KEY = "itemsRequest"
        const val READER_AUTH_KEY: String = "readerAuth"
    }
}

fun DocRequest.toDto(): DocRequestDto = DocRequestDto(
    itemsRequest = itemsRequest.toDto()
)
