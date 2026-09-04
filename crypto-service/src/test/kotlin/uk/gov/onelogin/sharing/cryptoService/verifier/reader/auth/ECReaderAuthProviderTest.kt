package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.SignatureException
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.internal.matchers.ThrowableCauseMatcher.hasCause
import org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.models.mdoc.exceptions.UnrecoverableError

class ECReaderAuthProviderTest {

    private var privateKeyOne: ECPrivateKey = mockk(relaxed = true)
    private var privateKeyTwo: ECPrivateKey = mockk(relaxed = true)

    private var certificateOne: X509Certificate = mockk(relaxed = true)
    private var certificateTwo: X509Certificate = mockk(relaxed = true)

    private var signature: Signature = mockk(relaxed = true)

    private val logger = SystemLogger()

    private var protectedHeaderGenerator: ProtectedHeaderGenerator = { byteArrayOf() }
    private var unprotectedHeaderGenerator: UnprotectedHeaderGenerator = { byteArrayOf() }

    private val provider by lazy {
        ECReaderAuthProvider(
            logger = logger,
            privateKeyChain = listOf(privateKeyOne, privateKeyTwo),
            certificateChain = listOf(certificateOne, certificateTwo),
            signature = signature,
            protectedHeaderGenerator = protectedHeaderGenerator,
            unprotectedHeaderGenerator = unprotectedHeaderGenerator
        )
    }

    @Test
    fun `Wraps InvalidKeyExceptions in UnrecoverableError instances`() = runTest {
        val invalidKeyException = InvalidKeyException("This is a unit test")
        every {
            signature.initSign(privateKeyTwo)
        } throws invalidKeyException

        val throwable = assertFails {
            provider.sign(byteArrayOf())
        }

        assertThat(
            throwable,
            allOf(
                instanceOf(UnrecoverableError::class.java),
                hasMessage(equalTo("Couldn't initialise signing with the provided Private Key.")),
                hasCause(equalTo(invalidKeyException))
            )
        )
    }

    @Test
    fun `Wraps SignatureExceptions in UnrecoverableError instances`() = runTest {
        val exception = SignatureException("This is a unit test")
        val input = byteArrayOf(1, 2, 3)
        every {
            signature.update(input)
        } throws exception

        val throwable = assertFails {
            provider.sign(input)
        }

        assertThat(
            throwable,
            allOf(
                instanceOf(UnrecoverableError::class.java),
                hasMessage(
                    equalTo("Couldn't create signature from provided reader authentication bytes.")
                ),
                hasCause(equalTo(exception))
            )
        )
    }

    @Test
    fun `Successfully signs provided byte array`() = runTest {
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        keyPairGenerator.initialize(256)
        val keyPair = keyPairGenerator.generateKeyPair()

        privateKeyTwo = keyPair.private as ECPrivateKey
        signature = Signature.getInstance("SHA256withECDSA")

        val result = provider.sign(byteArrayOf(1, 2, 3, 4, 5))

        assertNotNull(result)

        verify {
            privateKeyOne wasNot called
            certificateOne wasNot called
            certificateTwo wasNot called
        }
    }

    @Test
    fun `Logs a successfully created COSE_Sign1 structure`() = runTest {
        provider.sign(byteArrayOf())

        assertTrue {
            "Created COSE_Sign1 structure" in logger
        }
    }
}
