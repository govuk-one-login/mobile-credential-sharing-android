package uk.gov.onelogin.sharing.cryptoService.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.cryptoService.cose.Cose
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey

class CoseKeySerializer : StdSerializer<CoseKey>(CoseKey::class.java) {

    override fun serialize(value: CoseKey, gen: JsonGenerator, provider: SerializerProvider) {
        (gen as CBORGenerator).writeStartObject(FIELD_COUNT)

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

    private companion object {
        const val FIELD_COUNT = 4
    }
}
