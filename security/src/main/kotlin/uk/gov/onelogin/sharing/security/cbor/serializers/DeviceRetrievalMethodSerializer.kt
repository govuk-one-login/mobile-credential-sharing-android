package uk.gov.onelogin.sharing.security.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod

class DeviceRetrievalMethodSerializer :
    StdSerializer<DeviceRetrievalMethod>(DeviceRetrievalMethod::class.java) {
    override fun serialize(
        value: DeviceRetrievalMethod,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        gen.writeStartArray()
        gen.writeNumber(value.type)
        gen.writeNumber(value.version)
        provider.defaultSerializeValue(value.options, gen)
        gen.writeEndArray()
    }
}
