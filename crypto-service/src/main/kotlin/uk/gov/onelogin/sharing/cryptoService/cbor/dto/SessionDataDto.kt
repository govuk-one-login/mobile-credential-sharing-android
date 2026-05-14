package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
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
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

/**
 * ```
 * SessionData = {
 *     ? "data" : bstr
 *     ? "status" : uint
 *     * tstr => RFU
 * }
 * ```
 *
 * @param data The encrypted mdoc request / response. Defaults to `null`.
 * @param status The associated status code. Defaults to `null`.
 */
@Keep
@JsonSerialize(using = SessionDataDto.Serializer::class)
@JsonDeserialize(using = SessionDataDto.Deserializer::class)
data class SessionDataDto(
    @JsonProperty(KEY_DATA)
    val data: ByteArray? = null,
    @JsonProperty(KEY_STATUS)
    val status: UInt? = null
) {
    init {
        status?.let { code ->
            require(code in SessionDataStatus.applicableCodes) {
                "Received invalid session data status: $code"
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionDataDto

        if (!data.contentEquals(other.data)) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + status.hashCode()
        return result
    }

    @Keep
    class Serializer : StdSerializer<SessionDataDto>(SessionDataDto::class.java) {
        override fun serialize(
            value: SessionDataDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            require(gen is CBORGenerator) {
                "Attempted to serialize '${handledType().name}' without a CBORGenerator instance."
            }

            gen.run {
                val elementCount = listOfNotNull(value.data, value.status).size
                writeStartObject(elementCount)

                value.data?.let {
                    writeBinaryField(KEY_DATA, it)
                }
                value.status?.let {
                    writeNumberField(KEY_STATUS, it.toInt())
                }

                writeEndObject()
            }
        }
    }

    @Keep
    class Deserializer : StdDeserializer<SessionDataDto>(SessionDataDto::class.java) {
        override fun deserialize(
            jsonParser: JsonParser,
            context: DeserializationContext
        ): SessionDataDto = (jsonParser as CBORParser).use { parser ->
            val rootNode = CborMapper.default.readTree<JsonNode>(parser)
            var data: ByteArray? = null
            var status: UInt? = null

            if (rootNode.has(KEY_DATA)) {
                data = rootNode[KEY_DATA].binaryValue()
            }

            if (rootNode.has(KEY_STATUS)) {
                status = rootNode[KEY_STATUS].intValue().toUInt()
            }

            SessionDataDto(
                data = data,
                status = status
            )
        }
    }

    companion object {
        @Keep
        const val KEY_DATA: String = "data"

        @Keep
        const val KEY_STATUS: String = "status"
    }
}
