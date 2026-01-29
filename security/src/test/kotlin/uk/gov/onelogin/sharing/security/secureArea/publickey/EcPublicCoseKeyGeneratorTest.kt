package uk.gov.onelogin.sharing.security.secureArea.publickey

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.keypair.FakeKeyPairGenerator
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair
import uk.gov.onelogin.sharing.security.secureArea.privatekey.KeyPairToExceptions
import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(TestParameterInjector::class)
class EcPublicCoseKeyGeneratorTest {
    private var keyPair = validKeyPair

    private val keyPairGenerator by lazy {
        FakeKeyPairGenerator(keyPair)
    }

    private val generator by lazy {
        EcPublicCoseKeyGenerator(keyPairGenerator)
    }

    @Test
    fun `Valid CoseKeys obtained from generator`() = runTest {
        assertEquals(
            CoseKey.generateCoseKey(keyPair!!.public as ECPublicKey),
            generator.generateSessionPublicKey(),
        )
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
