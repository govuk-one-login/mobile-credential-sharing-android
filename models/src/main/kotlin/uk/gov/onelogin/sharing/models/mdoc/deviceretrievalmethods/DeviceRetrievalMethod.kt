package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer

sealed interface DeviceRetrievalMethod {
    val type: Int
    val version: Int
    val options: Any?
}

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
