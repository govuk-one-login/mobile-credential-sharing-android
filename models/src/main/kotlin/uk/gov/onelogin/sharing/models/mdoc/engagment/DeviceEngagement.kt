package uk.gov.onelogin.sharing.models.mdoc.engagment

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.security.Security

data class DeviceEngagement(
    val version: String,
    val security: Security,
    val deviceRetrievalMethods: List<DeviceRetrievalMethod>
)

class DeviceEngagementSerializer :
    StdSerializer<DeviceEngagement>(
        DeviceEngagement::class.java
    ) {
    override fun serialize(
        value: DeviceEngagement,
        gen: JsonGenerator,
        provider: SerializationContext
    ) {
        gen.writeStartObject()
        gen.writeName("0")
        gen.writeString(value.version)
        gen.writeName("1")
        provider.writeValue(gen, value.security)
        gen.writeName("2")
        gen.writeStartArray()
        value.deviceRetrievalMethods.forEach {
            provider.writeValue(gen, it)
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }
}

object DeviceEngagementCbor {
    val mapper: ObjectMapper = CborMappers.default()

    fun encode(deviceEngagement: DeviceEngagement): ByteArray =
        mapper.writeValueAsBytes(deviceEngagement)
}
