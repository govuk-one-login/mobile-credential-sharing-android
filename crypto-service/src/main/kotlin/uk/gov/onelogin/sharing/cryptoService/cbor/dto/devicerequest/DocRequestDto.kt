package uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest

import com.fasterxml.jackson.annotation.JsonIgnore
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
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest

@JsonSerialize(using = DocRequestDto.Serializer::class)
@JsonDeserialize(using = DocRequestDto.Deserializer::class)
data class DocRequestDto(
    val itemsRequest: ItemsRequestDto,
    @JsonIgnore
    val readerAuth: ByteArray? = null
) : CborEncodable {
    class Serializer : StdSerializer<DocRequestDto>(DocRequestDto::class.java) {
        override fun serialize(
            value: DocRequestDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldId(ITEMS_REQUEST_ID)
            gen.writeTag(EMBEDDED_CBOR_TAG)
            gen.writeBinary(CborMapper.default.writeValueAsBytes(value.itemsRequest))
            gen.writeEndObject()
        }

        private companion object {
            const val FIELD_COUNT = 1
            const val ITEMS_REQUEST_ID = 1L
        }
    }

    class Deserializer : JsonDeserializer<DocRequestDto>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DocRequestDto {
            val root = p.codec.readTree<JsonNode>(p)
            val itemsRequestNode = root["1"] ?: root["itemsRequest"]
                ?: throw IllegalArgumentException("Missing itemsRequest in DocRequest")
            val itemsRequest = CborMapper.default
                .readValue(itemsRequestNode.binaryValue(), ItemsRequestDto::class.java)
            return DocRequestDto(itemsRequest = itemsRequest)
        }
    }
}

fun DocRequest.toDto(): DocRequestDto = DocRequestDto(
    itemsRequest = itemsRequest.toDto()
)
