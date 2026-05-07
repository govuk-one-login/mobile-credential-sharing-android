package uk.gov.onelogin.sharing.cryptoService.cryptography.usecases

import java.security.interfaces.ECPrivateKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.cryptoService.cbor.decodeSessionEstablishmentModel
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.FakeDeviceRequestDecoder
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.cryptoService.toSessionEstablishment

class DecryptDeviceRequestUseCaseImplTest {
    private val logger = SystemLogger()
    private val sessionSecurity = FakeSessionSecurity()
    val fakeDecoder = FakeDeviceRequestDecoder(
        deviceRequestToReturn = deviceRequestStub
    )

    @Test
    fun `execute decrypts SessionEstablishment data and decodes DeviceRequest`() {
        val fakeSessionSecurity = FakeSessionSecurity().apply {
            plaintextToReturn = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        }

        val useCase = DecryptDeviceRequestUseCaseImpl(
            sessionSecurity = fakeSessionSecurity,
            deviceRequestDecoder = fakeDecoder,
            logger = logger
        )

        val sessionEstablishmentBytes = MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray()

        val engagement = "any-base64url-string"
        val keyPair = sessionSecurity.generateEcKeyPair(
            ELLIPTIC_CURVE_ALGORITHM,
            ELLIPTIC_CURVE_PARAMETER_SPEC
        )
        val holderPrivateKey = keyPair.private as ECPrivateKey

        var capturedSkDevice: ByteArray? = null
        val result = useCase.execute(
            sessionEstablishmentBytes = sessionEstablishmentBytes,
            engagement = engagement,
            holderPrivateKey = holderPrivateKey,
            decryptCounter = 1u,
            onDeriveSkDevice = { capturedSkDevice = it },
            onDeriveSessionTranscript = {}
        )

        val expectedCipherText = decodeSessionEstablishmentModel(
            rawBytes = sessionEstablishmentBytes,
            logger = logger
        ).toSessionEstablishment().data

        assertSame(fakeDecoder.deviceRequestToReturn, result)

        assertArrayEquals(
            fakeSessionSecurity.plaintextToReturn,
            fakeDecoder.lastPlaintext
        )

        assertArrayEquals(
            expectedCipherText,
            fakeSessionSecurity.lastDecryptData
        )
        assertSame(
            SessionKeyGenerator.Companion.DeviceRole.VERIFIER,
            fakeSessionSecurity.lastDecryptRole
        )

        assertEquals(
            1u,
            fakeSessionSecurity.lastDecryptCounter
        )

        assertArrayEquals(
            byteArrayOf(2),
            capturedSkDevice
        )
    }
}
