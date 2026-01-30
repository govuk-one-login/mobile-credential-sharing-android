package uk.gov.onelogin.sharing.security.secureArea.keypair

import java.security.interfaces.ECPublicKey
import junit.framework.TestCase
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.ALGORITHM
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.INVALID_ALGORITHM
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.INVALID_SPEC
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.PARAMETER_SPEC

class EcKeyPairGeneratorTest {
    val stubLogger = SystemLogger()
    val keyPairGenerator = EcKeyPairGenerator(stubLogger)

    @Test
    fun `generates valid public key`() {
        val publicKey = keyPairGenerator.generateEcKeyPair(
            ALGORITHM,
            PARAMETER_SPEC
        )
        TestCase.assertNotNull(publicKey)
    }

    @Test
    fun `generates public key using EC algorithm`() {
        val publicKey = keyPairGenerator.generateEcKeyPair(
            ALGORITHM,
            PARAMETER_SPEC
        )
        TestCase.assertEquals(ALGORITHM, publicKey?.public?.algorithm)
    }

    @Test
    fun `generates key with secp256r1 curve`() {
        val publicKey = keyPairGenerator.generateEcKeyPair(
            ALGORITHM,
            PARAMETER_SPEC
        )?.public as ECPublicKey

        val expectedParams = SessionSecurityTestStub.getKeyParameter()

        TestCase.assertEquals(expectedParams.curve, publicKey.params.curve)
    }

    @Test
    fun `returns null when NoSuchAlgorithmException is thrown`() {
        val publicKey = keyPairGenerator.generateEcKeyPair(
            INVALID_ALGORITHM,
            PARAMETER_SPEC
        )?.public?.let {
            it as ECPublicKey
        }

        TestCase.assertEquals(null, publicKey)
    }

    @Test
    fun `returns null when InvalidAlgorithmParameterException is thrown`() {
        val publicKey = keyPairGenerator.generateEcKeyPair(
            ALGORITHM,
            INVALID_SPEC
        )?.public?.let {
            it as ECPublicKey
        }

        TestCase.assertEquals(null, publicKey)
    }
}
