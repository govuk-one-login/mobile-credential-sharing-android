package uk.gov.onelogin.sharing.cryptoService.secureArea.session

import javax.crypto.AEADBadTagException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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

    @Test
    fun `encryptPayload produces output that can be decrypted back to original plaintext`() {
        val plaintext = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val holderSk = generateSessionKey(DeviceRole.HOLDER)

        val encrypted = aesEncryption.encryptPayload(
            key = holderSk,
            data = plaintext,
            role = DeviceRole.HOLDER,
            encryptCounter = 1u
        )

        val decrypted = aesEncryption.decryptPayload(
            key = holderSk,
            data = encrypted,
            role = DeviceRole.HOLDER,
            decryptCounter = 1u
        )

        assertContentEquals(plaintext, decrypted)
    }

    @Test
    fun `encryptPayload output length is plaintext plus 16 byte auth tag`() {
        val plaintext = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val holderSk = generateSessionKey(DeviceRole.HOLDER)

        val encrypted = aesEncryption.encryptPayload(
            key = holderSk,
            data = plaintext,
            role = DeviceRole.HOLDER,
            encryptCounter = 1u
        )

        assertEquals(plaintext.size + 16, encrypted.size)
    }

    @Test
    fun `encryptPayload with different counter produces different ciphertext`() {
        val plaintext = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val holderSk = generateSessionKey(DeviceRole.HOLDER)

        val encrypted1 = aesEncryption.encryptPayload(
            key = holderSk,
            data = plaintext,
            role = DeviceRole.HOLDER,
            encryptCounter = 1u
        )

        val encrypted2 = aesEncryption.encryptPayload(
            key = holderSk,
            data = plaintext,
            role = DeviceRole.HOLDER,
            encryptCounter = 2u
        )

        assertFalse(encrypted1.contentEquals(encrypted2))
    }

    @Test
    fun `encryptPayload ciphertext cannot be decrypted with wrong key`() {
        val plaintext = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val holderSk = generateSessionKey(DeviceRole.HOLDER)
        val readerSk = generateSessionKey(DeviceRole.VERIFIER)

        val encrypted = aesEncryption.encryptPayload(
            key = holderSk,
            data = plaintext,
            role = DeviceRole.HOLDER,
            encryptCounter = 1u
        )

        assertFailsWith(AEADBadTagException::class) {
            aesEncryption.decryptPayload(
                key = readerSk,
                data = encrypted,
                role = DeviceRole.HOLDER,
                decryptCounter = 1u
            )
        }
    }

    private fun assertLogFailure() {
        assert("session termination: status code 20" in logger)
        assert("session decryption error" in logger)
    }
}
