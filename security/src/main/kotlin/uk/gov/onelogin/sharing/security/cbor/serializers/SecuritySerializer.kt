package uk.gov.onelogin.sharing.security.cbor.serializers

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.security.Security

class SecuritySerializer : StdSerializer<Security>(Security::class.java) {
    override fun serialize(value: Security, gen: JsonGenerator, provider: SerializationContext) {
        gen.writeStartArray()
        gen.writeNumber(value.cipherSuiteIdentifier)
        val taggedBytes = EmbeddedCbor(value.eDeviceKeyBytes)
        provider.writeValue(gen, taggedBytes)
        gen.writeEndArray()
    }
}
