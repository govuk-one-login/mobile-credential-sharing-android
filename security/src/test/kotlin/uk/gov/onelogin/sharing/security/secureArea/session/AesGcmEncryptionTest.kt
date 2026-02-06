package uk.gov.onelogin.sharing.security.secureArea.session

import javax.crypto.AEADBadTagException
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import org.junit.Before
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.expectedSessionEstablishmentDto
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateSessionKey
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import uk.gov.onelogin.sharing.security.secureArea.session.SessionStubs.VALID_DECRYPTED_DATA_BYTES

class AesGcmEncryptionTest {

    private lateinit var logger: SystemLogger

    private lateinit var aesEncryption: AesGcmEncryption

    @Before
    fun setup() {
        logger = SystemLogger()
        aesEncryption = AesGcmEncryption(logger)
    }

    @Test
    fun `when correct key and role supplied, decryption matches given byte array`() {
        val data = expectedSessionEstablishmentDto.data.copyOf()
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
    fun `when correct key and role supplied, and decryption ran again, bytes do not match`() {
        val data = expectedSessionEstablishmentDto.data.copyOf()
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

        assertFailsWith(AEADBadTagException::class) {
            aesEncryption.decryptPayload(
                readerSk,
                data,
                DeviceRole.VERIFIER
            )
        }

        assertLogFailure()
    }

    @Test
    fun `when incorrect key supplied, decryption does not match given byte array`() {
        val data = expectedSessionEstablishmentDto.data.copyOf()
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

        assertLogFailure()
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

        assertLogFailure()
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

        assertLogFailure()
    }

    private fun assertLogFailure() {
        assert("session termination: status code 20" in logger)
        assert("session decryption error" in logger)
    }
}
