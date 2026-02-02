package uk.gov.onelogin.sharing.security.secureArea.publickey

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.keypair.FakeKeyPairGenerator
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair
import uk.gov.onelogin.sharing.security.secureArea.privatekey.KeyPairToExceptions

@RunWith(TestParameterInjector::class)
class EcPublicCoseKeyGeneratorTest {
    private var keyPair = validKeyPair

    private val keyPairGenerator by lazy {
        FakeKeyPairGenerator(keyPair)
    }
    private val logger = SystemLogger()

    private val generator by lazy {
        EcPublicCoseKeyGenerator(keyPairGenerator, logger)
    }

    @Test
    fun `Valid CoseKeys obtained from generator`() = runTest {
        val expected = CoseKey.generateCoseKey(keyPair!!.public as ECPublicKey)
        assertEquals(
            expected,
            generator.generateSessionPublicKey()
        )

        assert("Converted EC public key to CoseKey: $expected" in logger) {
            "Couldn't find expected message in logs: $logger"
        }
    }

    @Test
    @TestParameters(valuesProvider = KeyPairToExceptions::class)
    fun `Invalid KeyPair instances throw Exceptions`(
        keyPair: KeyPair,
        expectedExceptionClass: Class<out RuntimeException>
    ) = runTest {
        this@EcPublicCoseKeyGeneratorTest.keyPair = keyPair
        assertThrows(expectedExceptionClass) {
            generator.generateSessionPublicKey()
        }
    }
}
