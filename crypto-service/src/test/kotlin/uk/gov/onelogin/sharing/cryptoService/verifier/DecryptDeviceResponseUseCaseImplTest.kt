package uk.gov.onelogin.sharing.cryptoService.verifier

import javax.crypto.AEADBadTagException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionEncryption
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

class DecryptDeviceResponseUseCaseImplTest {
    private val logger = SystemLogger()
    private val fakeSessionEncryption = FakeSessionSecurity()

    private val useCase = DecryptDeviceResponseUseCaseImpl(
        sessionEncryption = fakeSessionEncryption,
        logger = logger
    )

    @Test
    fun `invoke decrypts device response bytes and returns plaintext`() {
        val expectedPlaintext = byteArrayOf(0x0A, 0x0B, 0x0C)
        fakeSessionEncryption.plaintextToReturn = expectedPlaintext

        val deviceResponseBytes = byteArrayOf(0x01, 0x02, 0x03)
        val skDevice = byteArrayOf(0x04, 0x05, 0x06)
        val encryptCounter = 3u

        val result = useCase(deviceResponseBytes, skDevice, encryptCounter)

        assertArrayEquals(expectedPlaintext, result)
        assertArrayEquals(deviceResponseBytes, fakeSessionEncryption.lastDecryptData)
        assertEquals(DeviceRole.HOLDER, fakeSessionEncryption.lastDecryptRole)
        assertEquals(encryptCounter, fakeSessionEncryption.lastDecryptCounter)
    }

    @Test
    fun `invoke throws DecryptDeviceResponseException when decryption fails`() {
        val throwingEncryption = object : SessionEncryption {
            override fun decryptPayload(
                key: ByteArray,
                data: ByteArray,
                role: DeviceRole,
                decryptCounter: UInt
            ): ByteArray = throw AEADBadTagException("decryption failed")

            override fun encryptPayload(
                key: ByteArray,
                data: ByteArray,
                role: DeviceRole,
                encryptCounter: UInt
            ): ByteArray = byteArrayOf()
        }

        val failingUseCase = DecryptDeviceResponseUseCaseImpl(
            sessionEncryption = throwingEncryption,
            logger = logger
        )

        val exception = assertThrows(DecryptDeviceResponseException::class.java) {
            failingUseCase(byteArrayOf(1), byteArrayOf(2), 1u)
        }

        assertEquals(DecryptDeviceResponseUseCaseImpl.LOG_DECRYPT_ERROR, exception.message)
    }
}
