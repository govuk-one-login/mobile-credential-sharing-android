package uk.gov.onelogin.sharing.cryptoService

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.MOCK_E_READER_KEY
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.cryptoService.cbor.decodeSessionEstablishmentModel
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionEstablishmentDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.SessionEstablishmentSerializer
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishment

class SessionEstablishmentMapperTest {

    private val logger = SystemLogger()

    private val eReaderKeyTagged = MOCK_E_READER_KEY.hexToByteArray()
    private val encryptedData = byteArrayOf(0x01, 0x02, 0x03)

    @Test
    fun `toDto strips Tag 24 so EmbeddedCborSerializer re-wraps correctly`() {
        val sessionEstablishment = SessionEstablishment(
            eReaderKey = eReaderKeyTagged,
            data = encryptedData
        )

        val dto = sessionEstablishment.toDto()

        // EmbeddedCbor should hold the untagged COSE key bytes (without D8 18 prefix)
        assertEquals(0xA4.toByte(), dto.eReaderKey.encoded[0])
    }

    @Test
    fun `round-trip toDto encode and decode preserves eReaderKey and data`() {
        val sessionEstablishment = SessionEstablishment(
            eReaderKey = eReaderKeyTagged,
            data = encryptedData
        )

        val encoded = sessionEstablishment.toDto().let { dto ->
            uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper.create(
                mapOf(
                    EmbeddedCbor::class.java
                        to EmbeddedCborSerializer(),
                    SessionEstablishmentDto::class.java
                        to SessionEstablishmentSerializer()
                )
            ).writeValueAsBytes(dto)
        }

        val roundTripped = decodeSessionEstablishmentModel(encoded, logger).toSessionEstablishment()

        assertArrayEquals(eReaderKeyTagged, roundTripped.eReaderKey)
        assertArrayEquals(encryptedData, roundTripped.data)
    }

    @Test
    fun `decoded SessionEstablishment eReaderKey matches MOCK_E_READER_KEY`() {
        val sessionEstablishment = decodeSessionEstablishmentModel(
            MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray(),
            logger
        ).toSessionEstablishment()

        assertArrayEquals(eReaderKeyTagged, sessionEstablishment.eReaderKey)
    }
}
