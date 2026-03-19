package uk.gov.onelogin.sharing.security.secureArea.secret

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.fail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.rsaKeyPair
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair

class EcdhSharedSecretGeneratorTest {
    private val logger = SystemLogger()

    private val generator by lazy {
        EcdhSharedSecretGenerator(logger)
    }

    @Test
    fun `Can generate shared secrets`() = runTest {
        val result = try {
            generator.generateSharedSecret(validKeyPair!!)
        } catch (e: Exception) {
            fail("Shouldn't have thrown an exception: ${e.message}")
        }

        assertNotNull(result)
    }

    @Test
    fun `Invalid KeyPair instances throw exceptions`() = runTest {
        assertThrows(ClassCastException::class.java) {
            generator.generateSharedSecret(rsaKeyPair)
        }
    }
}
