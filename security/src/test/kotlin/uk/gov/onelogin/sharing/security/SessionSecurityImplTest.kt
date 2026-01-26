package uk.gov.onelogin.sharing.security

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.ALGORITHM
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getKeyParameter
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getSharedSecret
import uk.gov.onelogin.sharing.security.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.security.cryptography.Constants.EC_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl
import uk.gov.onelogin.sharing.security.util.getByteArrayFromFile
import java.security.InvalidAlgorithmParameterException
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import kotlin.test.assertContentEquals


class SessionSecurityImplTest {
    val stubLogger = SystemLogger()
    val sessionSecurity = SessionSecurityImpl(stubLogger)

    @Test
    fun `generates valid public key`() {
        val publicKey = sessionSecurity.generateEcKeyPair(ALGORITHM, PARAMETER_SPEC)
        assertNotNull(publicKey)
    }

    @Test
    fun `generates public key using EC algorithm`() {
        val publicKey = sessionSecurity.generateEcKeyPair(ALGORITHM, PARAMETER_SPEC)
        assertEquals(ALGORITHM, publicKey?.public?.algorithm)
    }

    @Test
    fun `generates key with secp256r1 curve`() {
        val publicKey =
            sessionSecurity.generateEcKeyPair(ALGORITHM, PARAMETER_SPEC)?.public as ECPublicKey

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
    fun `generates correct session key from a given sharedKey and sessiontranscript with role SkReader`() {
        val skReaderKey = sessionSecurity.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            SK_READER_ROLE
        )

        assertContentEquals(skReaderKey, VALID_SKREADER_BYTES)
    }

    @Test
    fun `generates correct session key from a given sharedKey and sessiontranscript with role SkDevice`() {
        val skDeviceKey = sessionSecurity.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            SK_DEVICE_ROLE
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
            SK_DEVICE_ROLE
        )

        assert(!skDeviceKey.contentEquals(VALID_SKDEVICE_BYTES))
    }

    @Test
    fun `generated session key does not match when given sharedkey does not match`() {
        val holderSession = FakeSessionSecurity()
        val readerSession = FakeSessionSecurity()

        val readerKeyPair = readerSession.generateEcKeyPair(EC_ALGORITHM, EC_PARAMETER_SPEC)
        val holderKeyPair = holderSession.generateEcKeyPair(EC_ALGORITHM, EC_PARAMETER_SPEC)

        val sharedSecret = getSharedSecret(
            holderKeyPair.private as ECPrivateKey,
            readerKeyPair.public as ECPublicKey,
        )

        val skDeviceKey = sessionSecurity.deriveSessionKey(
            sharedSecret,
            validSessionTranscript,
            SK_DEVICE_ROLE
        )

        assert(!skDeviceKey.contentEquals(VALID_SKDEVICE_BYTES))
    }

    @Test
    fun `generated session key does not match when given role is wrong`() {
        val skReaderKey = sessionSecurity.deriveSessionKey(
            SHARED_SECRET_BYTES,
            validSessionTranscript,
            SK_DEVICE_ROLE
        )

        assert(!skReaderKey.contentEquals( VALID_SKREADER_BYTES))
    }

    @Test
    fun `derive session key throws exception when an invalid role is supplied and logs error`() {
        assertThrows(InvalidAlgorithmParameterException::class.java) {
            sessionSecurity.deriveSessionKey(
                SHARED_SECRET_BYTES,
                validSessionTranscript,
                INVALID_ROLE
            )
        }

        assert(
            INVALID_ROLE_ERROR_MESSAGE in stubLogger
        )
    }

    private companion object {
        const val SK_READER_ROLE = "SKReader"
        const val SK_DEVICE_ROLE = "SKDevice"
        const val INVALID_ROLE = "SKNull"
        const val INVALID_ROLE_ERROR_MESSAGE =
            "Invalid role string (status 10) supplied: $INVALID_ROLE"
        const val INVALID_ALGORITHM = "INVALID_ALGO"
        const val INVALID_SPEC = "INVALID_SPEC"

        const val CRYPTO_BINARY_PACKAGE_PATH =
            "src/testFixtures/resources/uk/gov/onelogin/sharing/security/cryptography/java/"
        const val SECURITY_BINARY_PACKAGE_PATH =
            "src/testFixtures/resources/uk/gov/onelogin/sharing/security/"

        val SHARED_SECRET_BYTES = getByteArrayFromFile(
            CRYPTO_BINARY_PACKAGE_PATH,
            "exampleSharedSecret.bin"
        )

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
