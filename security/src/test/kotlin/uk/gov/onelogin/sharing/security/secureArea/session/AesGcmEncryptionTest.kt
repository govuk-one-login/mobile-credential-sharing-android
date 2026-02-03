package uk.gov.onelogin.sharing.security.secureArea.session

import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.expectedSessionEstablishmentDto
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateSessionKey
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import uk.gov.onelogin.sharing.security.secureArea.session.SessionStubs.VALID_DECRYPTED_DATA_BYTES
import javax.crypto.AEADBadTagException
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class AesGcmEncryptionTest {

    private val logger = SystemLogger()

    private val aesEncryption = AesGcmEncryption(logger)

    @Test
    fun `when correct key and role supplied, decryption matches given byte array`() {
        val data = expectedSessionEstablishmentDto.data
        val readerSk = generateSessionKey(DeviceRole.VERIFIER)

        assertContentEquals(
            VALID_DECRYPTED_DATA_BYTES,
            aesEncryption.decryptPayload(
                readerSk,
                data,
                DeviceRole.VERIFIER
            )
        )

        assert("successful decryption" in logger)
    }

    @Test
    fun `when incorrect key supplied, decryption does not match given byte array`() {
        val data = expectedSessionEstablishmentDto.data
        val holderSk = generateSessionKey(DeviceRole.HOLDER)

        assertFailsWith(AEADBadTagException::class) {
            aesEncryption.decryptPayload(
                holderSk,
                data,
                DeviceRole.VERIFIER
            ).contentEquals(
                VALID_DECRYPTED_DATA_BYTES
            )
        }

        assert("session termination: status code 20" in logger)
        assert("session decryption error: authentication tag invalid" in logger)
    }

    @Test
    fun `when authentication tag is tampered, error thrown and logged`() {
        val data = expectedSessionEstablishmentDto.data.copyOf().apply {
            set(size - 1, 0)
        }
        val readerSk = generateSessionKey(DeviceRole.VERIFIER)

        assertFailsWith(AEADBadTagException::class) {
            aesEncryption.decryptPayload(
                readerSk,
                data,
                DeviceRole.VERIFIER
            )
        }

        assert("session termination: status code 20" in logger)
        assert("session decryption error: authentication tag invalid" in logger)
    }

    @Test
    fun `when data payload less than 16 bytes, error thrown and logged`() {
        val data = expectedSessionEstablishmentDto.data.copyOf().take(15).toByteArray()
        val readerSk = generateSessionKey(DeviceRole.VERIFIER)

        assertFailsWith(AEADBadTagException::class) {
            aesEncryption.decryptPayload(
                readerSk,
                data,
                DeviceRole.VERIFIER
            )
        }

        assert("session termination: status code 20" in logger)
        assert("session decryption error: payload too short for AES-256-GCM" in logger)
    }
}
