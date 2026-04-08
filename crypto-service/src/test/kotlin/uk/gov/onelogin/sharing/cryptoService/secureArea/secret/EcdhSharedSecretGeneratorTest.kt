package uk.gov.onelogin.sharing.cryptoService.secureArea.secret

import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.fail
import org.junit.Assert.assertThrows
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair

class EcdhSharedSecretGeneratorTest {
    private val logger = SystemLogger()

    private val generator by lazy {
        EcdhSharedSecretGenerator(logger)
    }

    @Test
    fun `Can generate shared secrets`() {
        val result = try {
            generator.generateSharedSecret(
                thisDevicePrivateKey = validKeyPair!!.private as ECPrivateKey,
                otherDevicePublicKey = validKeyPair!!.public as ECPublicKey
            )
        } catch (e: Exception) {
            fail("Shouldn't have thrown an exception: ${e.message}")
        }

        assertNotNull(result)
    }

    @Test
    fun `Mismatched EC curves throw InvalidKeyException`() {
        val p384KeyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp384r1"))
        }.generateKeyPair()

        assertThrows(InvalidKeyException::class.java) {
            generator.generateSharedSecret(
                thisDevicePrivateKey = validKeyPair!!.private as ECPrivateKey,
                otherDevicePublicKey = p384KeyPair.public as ECPublicKey
            )
        }
    }
}
