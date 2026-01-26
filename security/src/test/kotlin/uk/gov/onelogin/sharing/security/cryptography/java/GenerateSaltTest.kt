package uk.gov.onelogin.sharing.security.cryptography.java

import org.junit.Test
import uk.gov.onelogin.sharing.security.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.security.util.getByteArrayFromFile
import kotlin.test.assertContentEquals

class GenerateSaltTest {
    @Test
    fun `when generating salt from session transcript, a given exact bytearray is returned`() {
        val salt = generateSalt(validSessionTranscript)
        assertContentEquals(salt, VALID_SALT_BYTES)
    }

    @Test
    fun `when session transcript changes, bytearray does not match test reference`() {
        val salt = generateSalt(validSessionTranscript.apply {
            set(FIRST_BYTE, BYTE_ZERO)
        })
        assert(!salt.contentEquals(VALID_SALT_BYTES))
    }

    @Test
    fun `when session transcript is identical but with extra appended bytes, bytearray does not match test reference`() {
        val salt = generateSalt(validSessionTranscript + byteArrayOf(BYTE_ZERO))
        assert(!salt.contentEquals(VALID_SALT_BYTES))
    }

    companion object {
        val VALID_SALT_BYTES = getByteArrayFromFile(
            "src/testFixtures/resources/uk/gov/onelogin/sharing/security/cryptography/java/",
            "sessionTranscriptAsSaltBytes.bin"
        )

        const val FIRST_BYTE = 0
        const val BYTE_ZERO: Byte = 0x00
    }
}