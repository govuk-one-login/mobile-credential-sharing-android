package uk.gov.onelogin.sharing.models.mdoc.security

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.EmbeddedCbor

data class Security(val cipherSuiteIdentifier: Int, val eDeviceKeyBytes: EmbeddedCbor)

class SecuritySerializer : StdSerializer<Security>(Security::class.java) {
    override fun serialize(value: Security, gen: JsonGenerator, provider: SerializationContext) {
        gen.writeStartArray()
        gen.writeNumber(value.cipherSuiteIdentifier)
        provider.writeValue(gen, value.eDeviceKeyBytes)
        gen.writeEndArray()
    }
}
