package uk.gov.onelogin.sharing.cryptoService.secureArea.session

import javax.crypto.AEADBadTagException
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import org.junit.Before
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.expectedSessionEstablishmentDto
import uk.gov.onelogin.sharing.cryptoService.SessionSecurityTestStub.generateSessionKey
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionStubs.VALID_DECRYPTED_DATA_BYTES

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
                key = readerSk,
                data = data,
                role = DeviceRole.VERIFIER,
                decryptCounter = 1u
            )
        )

        assert("successful decryption" in logger)
    }

    @Test
    fun `throws exception when invalid decrypt counter provided`() {
        val data = expectedSessionEstablishmentDto.data.copyOf()
        val readerSk = generateSessionKey(DeviceRole.VERIFIER)
        val stubbedDecryptCounter = 1u
        val invalidDecryptCounter = 2u

        assertContentEquals(
            VALID_DECRYPTED_DATA_BYTES,
            aesEncryption.decryptPayload(
                key = readerSk,
                data = data,
                role = DeviceRole.VERIFIER,
                decryptCounter = stubbedDecryptCounter
            )
        )

        assert("successful decryption" in logger)

        assertFailsWith(AEADBadTagException::class) {
            aesEncryption.decryptPayload(
                key = readerSk,
                data = data,
                role = DeviceRole.VERIFIER,
                decryptCounter = invalidDecryptCounter
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
                key = holderSk,
                data = data,
                role = DeviceRole.VERIFIER,
                decryptCounter = 1u
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
                key = readerSk,
                data = data,
                role = DeviceRole.VERIFIER,
                decryptCounter = 1u
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
                key = readerSk,
                data = data,
                role = DeviceRole.VERIFIER,
                decryptCounter = 1u
            )
        }

        assertLogFailure()
    }

    private fun assertLogFailure() {
        assert("session termination: status code 20" in logger)
        assert("session decryption error" in logger)
    }
}
