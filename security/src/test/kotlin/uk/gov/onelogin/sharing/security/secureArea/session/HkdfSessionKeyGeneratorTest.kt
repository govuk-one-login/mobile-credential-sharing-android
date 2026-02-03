package uk.gov.onelogin.sharing.security.secureArea.session

import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import kotlin.test.assertContentEquals
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getSharedSecret
import uk.gov.onelogin.sharing.security.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.cryptography.java.CryptoStub.SHARED_SECRET_BYTES
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator.Companion.DeviceRole.HOLDER
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator.Companion.DeviceRole.VERIFIER
import uk.gov.onelogin.sharing.security.secureArea.session.SessionStubs.VALID_SKDEVICE_BYTES
import uk.gov.onelogin.sharing.security.secureArea.session.SessionStubs.VALID_SKREADER_BYTES

class HkdfSessionKeyGeneratorTest {

    private val logger = SystemLogger()

    private val sessionKeyGenerator = HkdfSessionKeyGenerator(logger)

    @Test
    fun `generates correct session key from a given sharedKey with role SkReader`() {
        val skReaderKey = sessionKeyGenerator.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            VERIFIER
        )

        assertContentEquals(skReaderKey, VALID_SKREADER_BYTES)
    }

    @Test
    fun `generates correct session key from a given sharedKey with role SkDevice`() {
        val skDeviceKey = sessionKeyGenerator.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            HOLDER
        )

        assertContentEquals(skDeviceKey, VALID_SKDEVICE_BYTES)
    }

    @Test
    fun `generated session key does not match when sessiontranscriptbytes is not identical`() {
        val skDeviceKey = sessionKeyGenerator.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript.copyOf().apply {
                set(0, 0x00)
            },
            HOLDER
        )

        assert(!skDeviceKey.contentEquals(VALID_SKDEVICE_BYTES))
    }

    @Test
    fun `generated session key does not match when given sharedkey does not match`() {
        val holderSession = FakeSessionSecurity()
        val readerSession = FakeSessionSecurity()

        val readerKeyPair = readerSession.generateEcKeyPair(
            ELLIPTIC_CURVE_ALGORITHM,
            ELLIPTIC_CURVE_PARAMETER_SPEC
        )
        val holderKeyPair = holderSession.generateEcKeyPair(
            ELLIPTIC_CURVE_ALGORITHM,
            ELLIPTIC_CURVE_PARAMETER_SPEC
        )

        val sharedSecret = getSharedSecret(
            holderKeyPair.private as ECPrivateKey,
            readerKeyPair.public as ECPublicKey
        )

        val skDeviceKey = sessionKeyGenerator.deriveSessionKey(
            sharedSecret,
            validSessionTranscript,
            HOLDER
        )

        assert(!skDeviceKey.contentEquals(VALID_SKDEVICE_BYTES))
    }

    @Test
    fun `generated session key does not match when given role is wrong`() {
        val skReaderKey = sessionKeyGenerator.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            HOLDER
        )

        assert(!skReaderKey.contentEquals(VALID_SKREADER_BYTES))
    }
}
