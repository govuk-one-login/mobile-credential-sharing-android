package uk.gov.onelogin.sharing.security.cryptography.java

import org.junit.Test
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getSharedSecret
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.util.getByteArrayFromFile
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import kotlin.test.assertContentEquals

class HkdfKeyGenerationTest {

    private val skDeviceAsBytes = "SKDevice".encodeCbor()
    private val skReaderAsBytes = "SKReader".encodeCbor()

    @Test
    fun `when generating key from shared key, salt and role as SkDevice, matches binary file resource`() {
        val generatedHkdfKey = hkdfKeyGeneration(
            SHARED_SECRET_BYTES,
            VALID_SALT_BYTES,
            skDeviceAsBytes
        )

        assertContentEquals(generatedHkdfKey, VALID_HKDF_DEVICE_KEY)
    }

    @Test
    fun `when generating key from shared key, salt and role as SkReader, matches binary file resource`() {
        val generatedHkdfKey = hkdfKeyGeneration(
            SHARED_SECRET_BYTES,
            VALID_SALT_BYTES,
            skReaderAsBytes
        )


        assertContentEquals(generatedHkdfKey, VALID_HKDF_READER_KEY)
    }

    @Test
    fun `when generating key from shared key, salt and role as SkReader, and ikm bytes are changed, bytes do not match binary file resource`() {
        val holderSession = FakeSessionSecurity()
        val readerSession = FakeSessionSecurity()

        val readerKeyPair = readerSession.generateEcKeyPair(EC_ALGORITHM, EC_PARAMETER_SPEC)
        val holderKeyPair = holderSession.generateEcKeyPair(EC_ALGORITHM, EC_PARAMETER_SPEC)

        val sharedSecret = getSharedSecret(
            holderKeyPair.private as ECPrivateKey,
            readerKeyPair.public as ECPublicKey,
        )

        val generatedHkdfKey = hkdfKeyGeneration(
            sharedSecret,
            VALID_SALT_BYTES,
            skReaderAsBytes
        )

        assert(!generatedHkdfKey.contentEquals(VALID_HKDF_READER_KEY))
    }

    @Test
    fun `when generating key from shared key, salt and role as SkReader, and salt bytes are changed, bytes do not match binary file resource`() {
        val generatedHkdfKey = hkdfKeyGeneration(
            SHARED_SECRET_BYTES,
            VALID_SALT_BYTES.apply {
                set(0, 0x00)
            },
            skReaderAsBytes
        )


        assert(!generatedHkdfKey.contentEquals(VALID_HKDF_READER_KEY))
    }

    @Test
    fun `when generating key from shared key, salt and wrong role given, bytes do not match binary file resource`() {
        val generatedHkdfKey = hkdfKeyGeneration(
            SHARED_SECRET_BYTES,
            VALID_SALT_BYTES,
            skReaderAsBytes
        )


        assert(!generatedHkdfKey.contentEquals(VALID_HKDF_DEVICE_KEY))
    }

    private companion object {

        const val BINARY_PACKAGE_PATH = "src/testFixtures/resources/uk/gov/onelogin/sharing/security/cryptography/java/"

        val VALID_SALT_BYTES = getByteArrayFromFile(
            BINARY_PACKAGE_PATH,
            "sessionTranscriptAsSaltBytes.bin"
        )

        val SHARED_SECRET_BYTES = getByteArrayFromFile(
            BINARY_PACKAGE_PATH,
            "exampleSharedSecret.bin"
        )

        val VALID_HKDF_DEVICE_KEY = getByteArrayFromFile(
            BINARY_PACKAGE_PATH,
            "hkdfDeviceKey.bin"
        )

        val VALID_HKDF_READER_KEY = getByteArrayFromFile(
            BINARY_PACKAGE_PATH,
            "hkdfReaderKey.bin"
        )
    }
}