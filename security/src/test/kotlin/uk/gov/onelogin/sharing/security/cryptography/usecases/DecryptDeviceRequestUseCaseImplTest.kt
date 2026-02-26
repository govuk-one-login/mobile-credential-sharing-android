package uk.gov.onelogin.sharing.security.cryptography.usecases

import java.security.interfaces.ECPrivateKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertSame
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.security.cbor.decodeSessionEstablishmentModel
import uk.gov.onelogin.sharing.security.cbor.decoders.FakeDeviceRequestDecoder
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.security.toSessionEstablishment

class DecryptDeviceRequestUseCaseImplTest {
    private val logger = SystemLogger()
    private val sessionSecurity = FakeSessionSecurity()
    val fakeDecoder = FakeDeviceRequestDecoder(
        deviceRequestToReturn = deviceRequestStub()
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

        val result = useCase.execute(
            sessionEstablishmentBytes = sessionEstablishmentBytes,
            engagement = engagement,
            holderPrivateKey = holderPrivateKey
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
    }
}
