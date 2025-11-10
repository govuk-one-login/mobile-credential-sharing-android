package uk.gov.onelogin.sharing.security.cbor.serializers

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement

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
        gen.writePropertyId(0)
        gen.writeString(value.version)
        gen.writePropertyId(1)
        provider.writeValue(gen, value.security)
        gen.writePropertyId(2)
        gen.writeStartArray()
        value.deviceRetrievalMethods.forEach {
            provider.writeValue(gen, it)
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }
}
