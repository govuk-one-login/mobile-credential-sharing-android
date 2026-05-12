package uk.gov.onelogin.sharing.cryptoService.verifier

import javax.crypto.AEADBadTagException
import kotlin.test.assertFailsWith
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.NIST_INITIALISATION_VECTOR_SIZE
import uk.gov.onelogin.sharing.cryptoService.cryptography.createNistInitialisationVector
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

class EncryptDeviceRequestUseCaseImplTest {

    private val logger = SystemLogger()
    private val plaintext = ByteArray(32) { it.toByte() }
    private val skReader = ByteArray(32) { 0x01 }
    private val encryptCounter = 1u
    private val encryptedResult = ByteArray(plaintext.size + 16) { 0xAA.toByte() }

    private val fakeSessionSecurity = FakeSessionSecurity().apply {
        encryptedToReturn = encryptedResult
    }

    private val useCase = EncryptDeviceRequestUseCaseImpl(fakeSessionSecurity, logger)

    @Test
    fun `IV is 12 bytes with 8 zero identifier bytes and counter 1 as last 4 bytes`() {
        val iv = createNistInitialisationVector(
            DeviceRole.VERIFIER.nistInitialisationVectorIdentifier,
            encryptCounter
        )

        assertEquals(NIST_INITIALISATION_VECTOR_SIZE, iv.size)
        assertArrayEquals(ByteArray(8) { 0x00 }, iv.take(8).toByteArray())
        assertArrayEquals(byteArrayOf(0x00, 0x00, 0x00, 0x01), iv.takeLast(4).toByteArray())
    }

    @Test
    fun `successful encryption returns ciphertext plus 16 byte auth tag`() {
        useCase(plaintext, skReader, encryptCounter)
        assertEquals(DeviceRole.VERIFIER, fakeSessionSecurity.lastEncryptRole)
        assertEquals(encryptCounter, fakeSessionSecurity.lastEncryptCounter)
        assertArrayEquals(skReader, fakeSessionSecurity.lastEncryptKey)
        assertTrue(logger.any { it.message == EncryptDeviceRequestUseCaseImpl.LOG_ENCRYPT_SUCCESS })
    }

    @Test
    fun `successful encryption logs counter incremented to 2`() {
        useCase(plaintext, skReader, encryptCounter)

        assertTrue(
            logger.any {
                it.message.startsWith("Message counter:") && it.message.endsWith(" 0x02")
            }
        )
    }

    @Test
    fun `encryption failure logs error and throws EncryptDeviceRequestException`() {
        fakeSessionSecurity.encryptException = AEADBadTagException("bad tag")

        assertFailsWith<EncryptDeviceRequestException> {
            useCase(plaintext, skReader, encryptCounter)
        }

        assertTrue(logger.any { it.message == EncryptDeviceRequestUseCaseImpl.LOG_ENCRYPT_ERROR })
    }
}
