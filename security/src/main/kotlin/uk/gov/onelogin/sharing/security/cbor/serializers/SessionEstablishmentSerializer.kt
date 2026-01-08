package uk.gov.onelogin.sharing.security.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.security.cbor.deserializers.E_READER_KEY
import uk.gov.onelogin.sharing.security.cbor.deserializers.SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.security.cbor.dto.SessionEstablishmentDto

class SessionEstablishmentSerializer :
    StdSerializer<SessionEstablishmentDto>(SessionEstablishmentDto::class.java) {
    override fun serialize(
        value: SessionEstablishmentDto,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        val cborGen = gen as? CBORGenerator
            ?: error("EmbeddedCbor requires CBORGenerator")

        cborGen.writeStartObject(2)

        cborGen.writeFieldName(E_READER_KEY)
        provider.defaultSerializeValue(value.eReaderKey, cborGen)

        cborGen.writeFieldName(SESSION_ESTABLISHMENT_DATA)
        cborGen.writeBinary(value.data)

        cborGen.writeEndObject()
    }
}
