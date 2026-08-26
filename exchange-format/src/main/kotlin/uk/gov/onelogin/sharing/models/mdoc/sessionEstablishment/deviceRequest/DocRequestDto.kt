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
import java.io.OutputStream
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.RawCbor

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
    val itemsRequestBytes: ByteArray? = null,
    @JsonIgnore
    val readerAuth: ByteArray? = null
) : CborEncodable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocRequestDto

        if (itemsRequest != other.itemsRequest) return false
        if (!itemsRequestBytes.contentEquals(other.itemsRequestBytes)) return false
        if (!readerAuth.contentEquals(other.readerAuth)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = itemsRequest.hashCode()
        result = 31 * result + (itemsRequestBytes?.contentHashCode() ?: 0)
        result = 31 * result + (readerAuth?.contentHashCode() ?: 0)
        return result
    }

    class Serializer : StdSerializer<DocRequestDto>(DocRequestDto::class.java) {
        override fun serialize(
            value: DocRequestDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            val cborGen = gen as CBORGenerator
            cborGen.writeStartObject()

            cborGen.writeFieldName(ITEMS_REQUEST_KEY)
            if (value.itemsRequestBytes != null) {
                provider.defaultSerializeValue(RawCbor(value.itemsRequestBytes), gen)
            } else {
                cborGen.writeTag(EMBEDDED_CBOR_TAG)
                cborGen.writeBinary(CborMapper.default.writeValueAsBytes(value.itemsRequest))
            }

            value.readerAuth?.let {
                cborGen.flush()
                val out = cborGen.outputTarget as OutputStream
                out.write(CBOR_TEXT_STRING_LENGTH_10)
                out.write(READER_AUTH_KEY.toByteArray())
                out.write(it)
            }

            cborGen.writeEndObject()
        }
    }

    class Deserializer : StdDeserializer<DocRequestDto>(DocRequestDto::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DocRequestDto {
            val root = p.codec.readTree<JsonNode>(p)
            val itemsRequestNode = root[ITEMS_REQUEST_KEY]
                ?: throw IllegalArgumentException("Missing itemsRequest in DocRequest")

            val itemsRequest = CborMapper.default
                .readValue(itemsRequestNode.binaryValue(), ItemsRequestDto::class.java)

            val readerAuthNode = root[READER_AUTH_KEY]
            val readerAuth = when {
                readerAuthNode == null -> null
                readerAuthNode.isBinary -> readerAuthNode.binaryValue()
                else -> CborMapper.default.writeValueAsBytes(readerAuthNode)
            }

            return DocRequestDto(
                itemsRequest = itemsRequest,
                itemsRequestBytes = null,
                readerAuth = readerAuth
            )
        }
    }

    companion object {
        const val ITEMS_REQUEST_KEY = "itemsRequest"
        const val READER_AUTH_KEY: String = "readerAuth"

        private const val CBOR_TEXT_STRING_LENGTH_10 = 0x6A
    }
}
