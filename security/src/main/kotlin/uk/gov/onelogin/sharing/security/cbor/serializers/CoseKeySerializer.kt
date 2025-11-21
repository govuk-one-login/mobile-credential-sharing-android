package uk.gov.onelogin.sharing.security.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.security.cose.Cose
import uk.gov.onelogin.sharing.security.cose.CoseKey

class CoseKeySerializer : StdSerializer<CoseKey>(CoseKey::class.java) {

    override fun serialize(value: CoseKey, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()

        gen.writeFieldId(Cose.KEY_KTY_LABEL)
        provider.defaultSerializeValue(value.keyType, gen)

        gen.writeFieldId(Cose.EC_CURVE_LABEL)
        gen.writeNumber(value.curve)

        gen.writeFieldId(Cose.EC_X_COORDINATE_LABEL)
        gen.writeBinary(value.x)

        gen.writeFieldId(Cose.EC_Y_COORDINATE_LABEL)
        gen.writeBinary(value.y)

        gen.writeEndObject()
    }
}
