package uk.gov.onelogin.sharing.security.cryptography.java

import kotlin.test.assertContentEquals
import org.junit.Test
import uk.gov.onelogin.sharing.security.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.security.cryptography.java.CryptoStub.VALID_SALT_BYTES
import uk.gov.onelogin.sharing.security.util.getByteArrayFromFile

class GenerateSaltTest {
    @Test
    fun `when generating salt from session transcript, a given exact bytearray is returned`() {
        val salt = generateSalt(validSessionTranscript)
        assertContentEquals(salt, VALID_SALT_BYTES)
    }

    @Test
    fun `when session transcript changes, bytearray does not match test reference`() {
        val salt = generateSalt(
            validSessionTranscript.copyOf().apply {
                set(FIRST_BYTE, BYTE_ZERO)
            }
        )
        assert(!salt.contentEquals(VALID_SALT_BYTES))
    }

    @Test
    fun `when session transcript has extra bytes, bytearray does not match test reference`() {
        val salt = generateSalt(validSessionTranscript + byteArrayOf(BYTE_ZERO))
        assert(!salt.contentEquals(VALID_SALT_BYTES))
    }

    companion object {
        const val FIRST_BYTE = 0
        const val BYTE_ZERO: Byte = 0x00
    }
}
