package uk.gov.onelogin.sharing.cryptoService.verifier

import javax.crypto.AEADBadTagException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.NIST_INITIALISATION_VECTOR_SIZE
import uk.gov.onelogin.sharing.cryptoService.cryptography.createNistInitialisationVector
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import kotlin.test.assertFailsWith

class EncryptDeviceRequestUseCaseImplTest {

    private val logger = SystemLogger()
    private val plaintext = ByteArray(32) { it.toByte() }
    private val skReader = ByteArray(32) { 0x01 }
    private val encryptCounter = 1u

    private val fakeEncryption = object : uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionEncryption {
        var lastKey: ByteArray? = null
        var lastData: ByteArray? = null
        var lastRole: DeviceRole? = null
        var lastCounter: UInt? = null
        var exceptionToThrow: Exception? = null
        val encryptedResult = ByteArray(plaintext.size + 16) { 0xAA.toByte() }

        override fun encryptPayload(
            key: ByteArray,
            data: ByteArray,
            role: DeviceRole,
            encryptCounter: UInt
        ): ByteArray {
            lastKey = key
            lastData = data
            lastRole = role
            lastCounter = encryptCounter
            exceptionToThrow?.let { throw it }
            return encryptedResult
        }

        override fun decryptPayload(
            key: ByteArray,
            data: ByteArray,
            role: DeviceRole,
            decryptCounter: UInt
        ): ByteArray = byteArrayOf()
    }

    private val useCase = EncryptDeviceRequestUseCaseImpl(fakeEncryption, logger)

    @Test
    fun `AC1 - IV is 12 bytes with 8 zero identifier bytes and counter 1 as last 4 bytes`() {
        val iv = createNistInitialisationVector(
            DeviceRole.VERIFIER.nistInitialisationVectorIdentifier,
            encryptCounter
        )

        assertEquals(NIST_INITIALISATION_VECTOR_SIZE, iv.size)
        assertArrayEquals(ByteArray(8) { 0x00 }, iv.take(8).toByteArray())
        assertArrayEquals(byteArrayOf(0x00, 0x00, 0x00, 0x01), iv.takeLast(4).toByteArray())
    }

    @Test
    fun `AC2 - successful encryption returns ciphertext plus 16 byte auth tag`() {
        val result = useCase.encrypt(plaintext, skReader, encryptCounter)

        assertEquals(plaintext.size + 16, result.size)
        assertEquals(DeviceRole.VERIFIER, fakeEncryption.lastRole)
        assertEquals(encryptCounter, fakeEncryption.lastCounter)
        assertArrayEquals(skReader, fakeEncryption.lastKey)
        assertTrue(logger.any { it.message == EncryptDeviceRequestUseCaseImpl.LOG_ENCRYPT_SUCCESS })
    }

    @Test
    fun `AC3 - successful encryption logs counter incremented to 2`() {
        useCase.encrypt(plaintext, skReader, encryptCounter)

        assertTrue(logger.any { it.message == "Message counter: 2" })
    }

    @Test
    fun `AC4 - encryption failure logs error and throws EncryptDeviceRequestException`() {
        fakeEncryption.exceptionToThrow = AEADBadTagException("bad tag")

        assertFailsWith<EncryptDeviceRequestException> {
            useCase.encrypt(plaintext, skReader, encryptCounter)
        }

        assertTrue(logger.any { it.message == EncryptDeviceRequestUseCaseImpl.LOG_ENCRYPT_ERROR })
    }

    @Test
    fun `AC4 - counter is not incremented on encryption failure`() {
        fakeEncryption.exceptionToThrow = AEADBadTagException("bad tag")
        val counterBefore = fakeEncryption.lastCounter

        runCatching { useCase.encrypt(plaintext, skReader, encryptCounter) }

        assertEquals(counterBefore, fakeEncryption.lastCounter)
    }
}
