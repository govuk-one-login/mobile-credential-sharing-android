package uk.gov.onelogin.sharing.security.cbor.serializers

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.security.cose.Cose
import uk.gov.onelogin.sharing.security.cose.CoseKey

class CoseKeySerializer : StdSerializer<CoseKey>(CoseKey::class.java) {

    override fun serialize(value: CoseKey, gen: JsonGenerator, provider: SerializationContext) {
        gen.writeStartObject()

        gen.writePropertyId(Cose.KEY_KTY_LABEL)
        provider.writeValue(gen, value.keyType)

        gen.writePropertyId(Cose.EC_CURVE_LABEL)
        gen.writeNumber(value.curve)

        gen.writePropertyId(Cose.EC_X_COORDINATE_LABEL)
        gen.writeBinary(value.x)

        gen.writePropertyId(Cose.EC_Y_COORDINATE_LABEL)
        gen.writeBinary(value.y)

        gen.writeEndObject()
    }
}
