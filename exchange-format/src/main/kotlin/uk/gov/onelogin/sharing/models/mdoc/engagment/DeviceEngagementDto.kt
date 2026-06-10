package uk.gov.onelogin.sharing.models.mdoc.engagment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
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
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toDto
import uk.gov.onelogin.sharing.models.mdoc.security.SecurityDto
import uk.gov.onelogin.sharing.models.mdoc.security.toDto

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = DeviceEngagementDto.Serializer::class)
@JsonDeserialize(using = DeviceEngagementDto.Deserializer::class)
data class DeviceEngagementDto(
    @JsonProperty(VERSION_KEY) val version: String,
    @JsonProperty(SECURITY_KEY) val security: SecurityDto,
    @JsonProperty(RETRIEVAL_METHODS_KEY) val deviceRetrievalMethods: List<DeviceRetrievalMethodDto>
) : CborEncodable {
    init {
        require(version.isNotEmpty()) { "DeviceEngagement: version must not be empty" }
        require(deviceRetrievalMethods.isNotEmpty()) {
            "DeviceEngagement: at least one retrieval method required"
        }
    }

    fun getFirstPeripheralServerModeUuid() = deviceRetrievalMethods.firstNotNullOfOrNull {
        it.getPeripheralServerModeUuid()
    }

    class Serializer : StdSerializer<DeviceEngagementDto>(DeviceEngagementDto::class.java) {
        override fun serialize(
            value: DeviceEngagementDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldId(VERSION_ID)
            gen.writeString(value.version)
            gen.writeFieldId(SECURITY_ID)
            provider.defaultSerializeValue(value.security, gen)
            gen.writeFieldId(RETRIEVAL_METHODS_ID)
            gen.writeStartArray(value.deviceRetrievalMethods, value.deviceRetrievalMethods.size)
            value.deviceRetrievalMethods.forEach { provider.defaultSerializeValue(it, gen) }
            gen.writeEndArray()
            gen.writeEndObject()
        }

        private companion object {
            const val FIELD_COUNT = 3
        }
    }

    class Deserializer : JsonDeserializer<DeviceEngagementDto>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DeviceEngagementDto {
            val root = p.codec.readTree<JsonNode>(p)
            val version = (root[VERSION_KEY] ?: root["version"])?.asText()
                ?: throw IllegalArgumentException("Missing version in DeviceEngagement")
            val security = p.codec.treeToValue(
                root[SECURITY_KEY] ?: root["security"],
                SecurityDto::class.java
            )
            val methodsNode = root[RETRIEVAL_METHODS_KEY] ?: root["deviceRetrievalMethods"]
                ?: throw IllegalArgumentException(
                    "Missing deviceRetrievalMethods in DeviceEngagement"
                )
            val methods = methodsNode.map {
                p.codec.treeToValue(it, DeviceRetrievalMethodDto::class.java)
            }
            return DeviceEngagementDto(version, security, methods)
        }
    }

    companion object {
        const val VERSION_KEY = "0"
        const val SECURITY_KEY = "1"
        const val RETRIEVAL_METHODS_KEY = "2"
        const val VERSION_ID = 0L
        const val SECURITY_ID = 1L
        const val RETRIEVAL_METHODS_ID = 2L
    }
}

fun DeviceEngagement.toDto(): DeviceEngagementDto = DeviceEngagementDto(
    version = version,
    security = security.toDto(),
    deviceRetrievalMethods = deviceRetrievalMethods
        .filterIsInstance<BleDeviceRetrievalMethod>()
        .map { it.toDto() }
)
