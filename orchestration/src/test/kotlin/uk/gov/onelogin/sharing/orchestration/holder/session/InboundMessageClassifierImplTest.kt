package uk.gov.onelogin.sharing.orchestration.holder.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDto
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto

class InboundMessageClassifierImplTest {
    private val logger = SystemLogger()
    private val classifier = InboundMessageClassifierImpl(logger)

    @Test
    fun `valid SessionEstablishment bytes are classified as SessionEstablishment`() {
        val sessionEstablishment = SessionEstablishmentDto(
            eReaderKey = EmbeddedCbor(byteArrayOf(0x01, 0x02, 0x03)),
            data = byteArrayOf(0x04, 0x05, 0x06)
        ).toCbor()

        val result = classifier.getMessageType(sessionEstablishment)

        assertEquals(InboundMessageType.SessionEstablishment, result)
    }

    @Test
    fun `status-only SessionData with status 20 is classified as StatusOnly`() {
        val statusOnlyBytes = SessionDataDto(data = null, status = 20u)
            .toCbor()

        val result = classifier.getMessageType(statusOnlyBytes)

        assertTrue(result is InboundMessageType.StatusOnly)
        assertEquals(
            SessionDataStatus.SESSION_TERMINATION,
            (result as InboundMessageType.StatusOnly).status
        )
    }

    @Test
    fun `SessionData with data and status is classified as Unknown`() {
        val dataWithStatus = SessionDataDto(
            data = byteArrayOf(0x01, 0x02, 0x03),
            status = 20u
        ).toCbor()

        val result = classifier.getMessageType(dataWithStatus)

        assertEquals(InboundMessageType.Unknown, result)
    }

    @Test
    fun `SessionData with data only is classified as Unknown`() {
        val dataOnly = SessionDataDto(
            data = byteArrayOf(0x01, 0x02, 0x03),
            status = null
        ).toCbor()

        val result = classifier.getMessageType(dataOnly)

        assertEquals(InboundMessageType.Unknown, result)
    }

    @Test
    fun `non-CBOR bytes are classified as Unknown`() {
        val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00)

        val result = classifier.getMessageType(invalidBytes)

        assertEquals(InboundMessageType.Unknown, result)
    }

    @Test
    fun `arbitrary CBOR map without known structure is classified as Unknown`() {
        val arbitraryBytes = CborMapper.default.writeValueAsBytes(
            mapOf("someKey" to byteArrayOf(0x01))
        )

        val result = classifier.getMessageType(arbitraryBytes)

        assertEquals(InboundMessageType.Unknown, result)
    }
}
