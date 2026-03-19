package uk.gov.onelogin.sharing.cryptoService.cryptography.java

import java.security.MessageDigest
import kotlin.test.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_TRANSCRIPT
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.CryptoStub.VALID_SALT

class GenerateSaltTest {
    @Test
    fun `when generating salt from session transcript, a given exact bytearray is returned`() {
        val transcriptBytes = VALID_TRANSCRIPT.hexToByteArray()
        val salt = generateSalt(transcriptBytes)

        assertEquals(
            VALID_SALT,
            salt.toHexString()
        )
    }

    @Test
    fun `salt input is tag24 bstr of transcript`() {
        val transcriptBytes = VALID_TRANSCRIPT.hexToByteArray()
        val saltInput = EmbeddedCbor(transcriptBytes).encodeCbor()

        assertEquals(0xD8.toByte(), saltInput[0])
        assertEquals(0x18.toByte(), saltInput[1])

        val salt = MessageDigest.getInstance("SHA-256").digest(saltInput)
        assertEquals(VALID_SALT, salt.toHexString())
    }

    @Test
    fun `when session transcript changes, bytearray does not match test reference`() {
        val transcriptBytes = VALID_TRANSCRIPT.hexToByteArray()
        val mutated = validSessionTranscript.copyOf().apply {
            set(FIRST_BYTE, BYTE_ZERO)
        }

        assertNotEquals(
            generateSalt(transcriptBytes),
            generateSalt(mutated)
        )
    }

    @Test
    fun `when session transcript has extra bytes, bytearray does not match test reference`() {
        val salt = generateSalt(validSessionTranscript + byteArrayOf(BYTE_ZERO))
        assertNotEquals(VALID_SALT, salt.toHexString())
    }

    companion object {
        const val FIRST_BYTE = 0
        const val BYTE_ZERO: Byte = 0x00
    }
}
