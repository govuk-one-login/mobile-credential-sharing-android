package uk.gov.onelogin.sharing.cryptoService.verifier

import javax.crypto.AEADBadTagException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceResponseDecoder
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceResponseDecodingException
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionEncryption
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

class DecryptDeviceResponseUseCaseImplTest {
    private val logger = SystemLogger()
    private val fakeSessionEncryption = FakeSessionSecurity()
    private val expectedResponse = DeviceResponse()
    private val fakeDecoder = DeviceResponseDecoder { expectedResponse }

    private val useCase = DecryptDeviceResponseUseCaseImpl(
        sessionEncryption = fakeSessionEncryption,
        deviceResponseDecoder = fakeDecoder,
        logger = logger
    )

    @Test
    fun `invoke decrypts and decodes device response returning domain model`() {
        fakeSessionEncryption.plaintextToReturn = byteArrayOf(0x0A, 0x0B, 0x0C)

        val result = useCase(
            deviceResponseBytes = byteArrayOf(0x01, 0x02, 0x03),
            skDevice = byteArrayOf(0x04, 0x05, 0x06),
            encryptCounter = 3u
        )

        assertSame(expectedResponse, result)
        assertEquals(DeviceRole.HOLDER, fakeSessionEncryption.lastDecryptRole)
        assertEquals(3u, fakeSessionEncryption.lastDecryptCounter)
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
            deviceResponseDecoder = fakeDecoder,
            logger = logger
        )

        val exception = assertFailsWith<DecryptDeviceResponseException> {
            failingUseCase(byteArrayOf(1), byteArrayOf(2), 1u)
        }

        assertEquals(DecryptDeviceResponseUseCaseImpl.LOG_DECRYPT_ERROR, exception.message)
    }

    @Test
    fun `invoke throws DecryptDeviceResponseException when decoding fails`() {
        fakeSessionEncryption.plaintextToReturn = byteArrayOf(0x0A)
        val throwingDecoder = DeviceResponseDecoder {
            throw DeviceResponseDecodingException("bad cbor")
        }

        val failingUseCase = DecryptDeviceResponseUseCaseImpl(
            sessionEncryption = fakeSessionEncryption,
            deviceResponseDecoder = throwingDecoder,
            logger = logger
        )

        val exception = assertFailsWith<DecryptDeviceResponseException> {
            failingUseCase(byteArrayOf(1), byteArrayOf(2), 1u)
        }

        assertEquals(DecryptDeviceResponseUseCaseImpl.LOG_DECRYPT_ERROR, exception.message)
    }
}
