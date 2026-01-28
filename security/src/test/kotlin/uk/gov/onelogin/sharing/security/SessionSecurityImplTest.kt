package uk.gov.onelogin.sharing.security

import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlin.test.assertContentEquals
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.ALGORITHM
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getKeyParameter
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getSharedSecret
import uk.gov.onelogin.sharing.security.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.cryptography.java.CryptoStub.SHARED_SECRET_BYTES
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity.Companion.DeviceRole.HOLDER
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity.Companion.DeviceRole.VERIFIER
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl
import uk.gov.onelogin.sharing.security.util.getByteArrayFromFile

class SessionSecurityImplTest {
    val stubLogger = SystemLogger()
    val sessionSecurity = SessionSecurityImpl(stubLogger)

    @Test
    fun `generates valid public key`() {
        val publicKey = sessionSecurity.generateEcKeyPair(
            ALGORITHM,
            PARAMETER_SPEC
        )
        assertNotNull(publicKey)
    }

    @Test
    fun `generates public key using EC algorithm`() {
        val publicKey = sessionSecurity.generateEcKeyPair(
            ALGORITHM,
            PARAMETER_SPEC
        )
        assertEquals(ALGORITHM, publicKey?.public?.algorithm)
    }

    @Test
    fun `generates key with secp256r1 curve`() {
        val publicKey = sessionSecurity.generateEcKeyPair(
            ALGORITHM,
            PARAMETER_SPEC
        )?.public as ECPublicKey

        val expectedParams = getKeyParameter()

        assertEquals(expectedParams.curve, publicKey.params.curve)
    }

    @Test
    fun `returns null when NoSuchAlgorithmException is thrown`() {
        val publicKey = SessionSecurityTestStub.sessionSecurity.generateEcKeyPair(
            INVALID_ALGORITHM,
            PARAMETER_SPEC
        )?.public?.let {
            it as ECPublicKey
        }

        assertEquals(null, publicKey)
    }

    @Test
    fun `returns null when InvalidAlgorithmParameterException is thrown`() {
        val publicKey = SessionSecurityTestStub.sessionSecurity.generateEcKeyPair(
            ALGORITHM,
            INVALID_SPEC
        )?.public?.let {
            it as ECPublicKey
        }

        assertEquals(null, publicKey)
    }

    @Test
    fun `generates correct session key from a given sharedKey with role SkReader`() {
        val skReaderKey = sessionSecurity.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            VERIFIER
        )

        assertContentEquals(skReaderKey, VALID_SKREADER_BYTES)
    }

    @Test
    fun `generates correct session key from a given sharedKey with role SkDevice`() {
        val skDeviceKey = sessionSecurity.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            HOLDER
        )

        assertContentEquals(skDeviceKey, VALID_SKDEVICE_BYTES)
    }

    @Test
    fun `generated session key does not match when sessiontranscriptbytes is not identical`() {
        val skDeviceKey = sessionSecurity.deriveSessionKey(
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

        val skDeviceKey = sessionSecurity.deriveSessionKey(
            sharedSecret,
            validSessionTranscript,
            HOLDER
        )

        assert(!skDeviceKey.contentEquals(VALID_SKDEVICE_BYTES))
    }

    @Test
    fun `generated session key does not match when given role is wrong`() {
        val skReaderKey = sessionSecurity.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            HOLDER
        )

        assert(!skReaderKey.contentEquals(VALID_SKREADER_BYTES))
    }

    private companion object {
        const val INVALID_ALGORITHM = "INVALID_ALGO"
        const val INVALID_SPEC = "INVALID_SPEC"

        const val SECURITY_BINARY_PACKAGE_PATH =
            "src/testFixtures/resources/uk/gov/onelogin/sharing/security/"

        val VALID_SKREADER_BYTES = getByteArrayFromFile(
            SECURITY_BINARY_PACKAGE_PATH,
            "validSkReaderKey.bin"
        )

        val VALID_SKDEVICE_BYTES = getByteArrayFromFile(
            SECURITY_BINARY_PACKAGE_PATH,
            "validSkDeviceKey.bin"
        )
    }
}
