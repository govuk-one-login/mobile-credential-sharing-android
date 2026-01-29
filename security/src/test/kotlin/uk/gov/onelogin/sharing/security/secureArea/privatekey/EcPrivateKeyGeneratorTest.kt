package uk.gov.onelogin.sharing.security.secureArea.privatekey

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator
import uk.gov.onelogin.sharing.security.secureArea.keypair.FakeKeyPairGenerator
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair
import java.security.KeyPair
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(TestParameterInjector::class)
class EcPrivateKeyGeneratorTest {

    private var keyPair = validKeyPair

    private val keyPairGenerator: KeyGenerator.KeyPairGenerator by lazy {
        FakeKeyPairGenerator(keyPair)
    }

    private val generator by lazy {
        EcPrivateKeyGenerator(keyPairGenerator)
    }

    @Test
    fun `EC Private keys are cast from the private key generator`() = runTest {
        assertEquals(
            keyPair!!.private,
            generator.getSessionPrivateKey()
        )
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

