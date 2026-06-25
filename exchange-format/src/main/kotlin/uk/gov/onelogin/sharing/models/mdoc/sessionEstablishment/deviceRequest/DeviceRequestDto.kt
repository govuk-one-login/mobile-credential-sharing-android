package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

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
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable

/**
 * ```
 * DeviceRequest = {
 *    "version" : Version,
 *    "docRequests" : [+ DocRequest],
 *    ? "deviceRequestInfo" : DeviceRequestInfoBytes,
 *    ? "readerAuthAll" : [+ReaderAuthAll],
 *    * tstr => RFU
 * }
 * ```
 */
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceRequestDto

        if (version != other.version) return false
        if (docRequest != other.docRequest) return false
        if (!deviceRequestInfo.contentEquals(other.deviceRequestInfo)) return false
        if (!readerAuthAll.contentEquals(other.readerAuthAll)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + docRequest.hashCode()
        result = 31 * result + (deviceRequestInfo?.contentHashCode() ?: 0)
        result = 31 * result + (readerAuthAll?.contentHashCode() ?: 0)
        return result
    }

    class Serializer : StdSerializer<DeviceRequestDto>(DeviceRequestDto::class.java) {
        override fun serialize(
            value: DeviceRequestDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldName(VERSION_KEY)
            gen.writeString(value.version)
            gen.writeFieldName(DOC_REQUESTS_KEY)
            gen.writeStartArray(value.docRequest, value.docRequest.size)
            value.docRequest.forEach { provider.defaultSerializeValue(it, gen) }
            gen.writeEndArray()
            gen.writeEndObject()
        }
    }

    class Deserializer : JsonDeserializer<DeviceRequestDto>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DeviceRequestDto {
            val root = p.codec.readTree<JsonNode>(p)
            val version = root[VERSION_KEY]?.asText()
                ?: throw IllegalArgumentException("Missing version in DeviceRequest")
            val docRequests = root[DOC_REQUESTS_KEY]
                ?.map { p.codec.treeToValue(it, DocRequestDto::class.java) }
                ?: emptyList()
            return DeviceRequestDto(version = version, docRequest = docRequests)
        }
    }

    companion object {
        private const val FIELD_COUNT = 2
        const val VERSION_KEY = "version"
        const val DOC_REQUESTS_KEY = "docRequests"
        const val DEVICE_REQUEST_INFO_KEY: String = "deviceRequestInfo"
        const val READER_AUTH_ALL_KEY: String = "readerAuthAll"
    }

    fun toDomain(): DeviceRequest = DeviceRequest(
        version = version,
        docRequests = docRequest.map {
            DocRequest(ItemsRequest(it.itemsRequest.docType, it.itemsRequest.nameSpaces))
        }
    )
}
