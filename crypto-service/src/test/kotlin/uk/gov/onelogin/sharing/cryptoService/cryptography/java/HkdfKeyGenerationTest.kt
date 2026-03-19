package uk.gov.onelogin.sharing.cryptoService.cryptography.java

import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import kotlin.test.assertContentEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.SessionSecurityTestStub.getSharedSecret
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.CryptoStub.SHARED_SECRET_BYTES
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.CryptoStub.VALID_HKDF_DEVICE_KEY
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.CryptoStub.VALID_HKDF_READER_KEY
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.CryptoStub.VALID_SALT_BYTES

class HkdfKeyGenerationTest {

    private val skDeviceAsBytes = "SKDevice".encodeCbor()
    private val skReaderAsBytes = "SKReader".encodeCbor()

    @Test
    fun `when generating key with role as SkDevice, matches binary file resource`() {
        val generatedHkdfKey = hkdfKeyGeneration(
            SHARED_SECRET_BYTES,
            VALID_SALT_BYTES,
            skDeviceAsBytes
        )

        assertContentEquals(generatedHkdfKey, VALID_HKDF_DEVICE_KEY)
    }

    @Test
    fun `when generating key with role as SkReader, matches binary file resource`() {
        val generatedHkdfKey = hkdfKeyGeneration(
            SHARED_SECRET_BYTES,
            VALID_SALT_BYTES,
            skReaderAsBytes
        )

        assertContentEquals(generatedHkdfKey, VALID_HKDF_READER_KEY)
    }

    @Test
    fun `when generating key and ikm changed, bytes do not match binary file resource`() {
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

        val generatedHkdfKey = hkdfKeyGeneration(
            sharedSecret,
            VALID_SALT_BYTES,
            skReaderAsBytes
        )

        assert(!generatedHkdfKey.contentEquals(VALID_HKDF_READER_KEY))
    }

    @Test
    fun `when generating key and salt changed, bytes do not match binary file resource`() {
        val generatedHkdfKey = hkdfKeyGeneration(
            SHARED_SECRET_BYTES,
            VALID_SALT_BYTES.copyOf().apply {
                set(0, 0x00)
            },
            skReaderAsBytes
        )

        assert(!generatedHkdfKey.contentEquals(VALID_HKDF_READER_KEY))
    }

    @Test
    fun `when generating key and wrong role given, bytes dont match binary file resource`() {
        val generatedHkdfKey = hkdfKeyGeneration(
            SHARED_SECRET_BYTES,
            VALID_SALT_BYTES,
            skReaderAsBytes
        )

        assert(!generatedHkdfKey.contentEquals(VALID_HKDF_DEVICE_KEY))
    }
}
