package uk.gov.onelogin.sharing.security.cbor.serializers

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions

const val PROPERTY_ID_0: Long = 0
const val PROPERTY_ID_1: Long = 1
const val PROPERTY_ID_10: Long = 10

class BleOptionsSerializer : StdSerializer<BleOptions>(BleOptions::class.java) {
    override fun serialize(value: BleOptions, gen: JsonGenerator, provider: SerializationContext) {
        gen.writeStartObject()
        gen.writePropertyId(PROPERTY_ID_0)
        gen.writeBoolean(value.serverMode)
        gen.writePropertyId(PROPERTY_ID_1)
        gen.writeBoolean(value.clientMode)
        gen.writePropertyId(PROPERTY_ID_10)
        provider.writeValue(gen, value.peripheralServerModeUuid)
        gen.writeEndObject()
    }
}
