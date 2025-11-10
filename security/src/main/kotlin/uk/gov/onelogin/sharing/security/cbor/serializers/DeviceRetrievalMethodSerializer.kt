package uk.gov.onelogin.sharing.security.cbor.serializers

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod

class DeviceRetrievalMethodSerializer :
    StdSerializer<DeviceRetrievalMethod>(DeviceRetrievalMethod::class.java) {
    override fun serialize(
        value: DeviceRetrievalMethod,
        gen: JsonGenerator,
        provider: SerializationContext
    ) {
        gen.writeStartArray()
        gen.writeNumber(value.type)
        gen.writeNumber(value.version)
        provider.writeValue(gen, value.options)
        gen.writeEndArray()
    }
}
