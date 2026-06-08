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
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

@JsonSerialize(using = DeviceRequestDto.Serializer::class)
@JsonDeserialize(using = DeviceRequestDto.Deserializer::class)
data class DeviceRequestDto(
    val version: String,
    val docRequest: List<DocRequestDto>,
    @JsonIgnore
    val deviceRequestInfo: ByteArray? = null,
    @JsonIgnore
    val readerAuthAll: ByteArray? = null
) : CborEncodable {
    init {
        require(version.isNotEmpty()) { "DeviceRequest: version must not be empty" }
    }

    class Serializer : StdSerializer<DeviceRequestDto>(DeviceRequestDto::class.java) {
        override fun serialize(
            value: DeviceRequestDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldId(VERSION_ID)
            gen.writeString(value.version)
            gen.writeFieldId(DOC_REQUESTS_ID)
            gen.writeStartArray(value.docRequest, value.docRequest.size)
            value.docRequest.forEach { provider.defaultSerializeValue(it, gen) }
            gen.writeEndArray()
            gen.writeEndObject()
        }

        private companion object {
            const val FIELD_COUNT = 2
            const val VERSION_ID = 0L
            const val DOC_REQUESTS_ID = 1L
        }
    }

    class Deserializer : JsonDeserializer<DeviceRequestDto>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DeviceRequestDto {
            val root = p.codec.readTree<JsonNode>(p)
            val version = (root[VERSION_KEY] ?: root["version"])?.asText()
                ?: throw IllegalArgumentException("Missing version in DeviceRequest")
            val docRequests = (root[DOC_REQUESTS_KEY] ?: root["docRequests"])
                ?.map { p.codec.treeToValue(it, DocRequestDto::class.java) }
                ?: emptyList()
            return DeviceRequestDto(version = version, docRequest = docRequests)
        }
    }

    companion object {
        const val VERSION_KEY = "0"
        const val DOC_REQUESTS_KEY = "1"
        const val VERSION_ID = 0L
        const val DOC_REQUESTS_ID = 1L
    }

    fun toDomain(): DeviceRequest = DeviceRequest(
        version = version,
        docRequests = docRequest.map {
            DocRequest(ItemsRequest(it.itemsRequest.docType, it.itemsRequest.nameSpaces))
        }
    )
}

fun DeviceRequest.toDto(): DeviceRequestDto = DeviceRequestDto(
    version = version,
    docRequest = docRequests.map { it.toDto() }
)
