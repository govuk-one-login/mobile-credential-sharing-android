package uk.gov.onelogin.sharing.security.secureArea.privatekey

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import java.security.KeyPair
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator
import uk.gov.onelogin.sharing.security.secureArea.keypair.FakeKeyPairGenerator
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair

@RunWith(TestParameterInjector::class)
class EcPrivateKeyGeneratorTest {

    private var keyPair = validKeyPair

    private val keyPairGenerator: KeyGenerator.KeyPairGenerator by lazy {
        FakeKeyPairGenerator(keyPair)
    }

    private val logger = SystemLogger()

    private val generator by lazy {
        EcPrivateKeyGenerator(keyPairGenerator, logger)
    }

    @Test
    fun `EC Private keys are cast from the private key generator`() = runTest {
        assertEquals(
            keyPair!!.private,
            generator.getSessionPrivateKey()
        )

        assert("Obtained EC Private key from KeyPairGenerator" in logger) {
            "Couldn't find expected message in logs: $logger"
        }
    }

    @Test
    @TestParameters(valuesProvider = KeyPairToExceptions::class)
    fun `Invalid KeyPair instances throw Exceptions`(
        keyPair: KeyPair,
        expectedExceptionClass: Class<out RuntimeException>
    ) = runTest {
        this@EcPrivateKeyGeneratorTest.keyPair = keyPair
        assertThrows(expectedExceptionClass) {
            generator.getSessionPrivateKey()
        }
    }
}
