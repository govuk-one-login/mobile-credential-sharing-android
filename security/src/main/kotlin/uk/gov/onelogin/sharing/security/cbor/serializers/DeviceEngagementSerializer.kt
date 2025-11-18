package uk.gov.onelogin.sharing.security.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement

class DeviceEngagementSerializer :
    StdSerializer<DeviceEngagement>(
        DeviceEngagement::class.java
    ) {
    override fun serialize(
        value: DeviceEngagement,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldId(0)
        gen.writeString(value.version)
        gen.writeFieldId(1)
        provider.defaultSerializeValue(value.security, gen)
        gen.writeFieldId(2)
        gen.writeStartArray()
        value.deviceRetrievalMethods.forEach {
            provider.defaultSerializeValue(it, gen)
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }
}
