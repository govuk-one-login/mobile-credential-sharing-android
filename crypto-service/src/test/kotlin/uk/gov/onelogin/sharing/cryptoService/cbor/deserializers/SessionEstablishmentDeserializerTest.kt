package uk.gov.onelogin.sharing.cryptoService.cbor.deserializers

import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto

class SessionEstablishmentDeserializerTest {

    @Test
    fun `should prefix eReaderKey with CBOR Tag 24 after deserializing raw bytes`() {
        val cborData = MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray()

        val dto = CborMapper.default.readValue(cborData, SessionEstablishmentDto::class.java)

        val eReaderKeyBytes = CborMapper.default.writeValueAsBytes(
            EmbeddedCbor(dto.eReaderKey.encoded)
        )

        assertEquals(0xD8.toByte(), eReaderKeyBytes[0])
        assertEquals(0x18.toByte(), eReaderKeyBytes[1])
    }
}
