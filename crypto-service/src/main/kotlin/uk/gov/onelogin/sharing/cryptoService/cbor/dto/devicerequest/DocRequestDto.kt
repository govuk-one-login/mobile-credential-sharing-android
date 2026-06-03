package uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.deserializers.ItemsRequestDeserializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest

@JsonSerialize(using = DocRequestDto.Serializer::class)
data class DocRequestDto(
    @JsonProperty("itemsRequest")
    @JsonDeserialize(using = ItemsRequestDeserializer::class)
    val itemsRequest: ItemsRequestDto,
    @JsonIgnore
    val readerAuth: ByteArray? = null
) {
    class Serializer : StdSerializer<DocRequestDto>(DocRequestDto::class.java) {
        override fun serialize(
            value: DocRequestDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(1)
            gen.writeFieldName("itemsRequest")
            gen.writeTag(EMBEDDED_CBOR_TAG)
            gen.writeBinary(CborMapper.default.writeValueAsBytes(value.itemsRequest))
            gen.writeEndObject()
        }
    }
}

fun DocRequest.toDto(): DocRequestDto = DocRequestDto(
    itemsRequest = itemsRequest.toDto()
)
