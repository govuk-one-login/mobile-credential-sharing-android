package uk.gov.onelogin.sharing.orchestration.verifier.auth.reader

import io.mockk.every
import io.mockk.mockk
import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.ECPrivateKey
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.internal.matchers.ThrowableCauseMatcher.hasCause
import org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage
import uk.gov.onelogin.sharing.orchestration.exceptions.UnrecoverableError

class ECReaderAuthProviderTest {

    private var privateKey: ECPrivateKey = mockk(relaxed = true)
    private var signature: Signature = mockk(relaxed = true)

    private val provider by lazy {
        ECReaderAuthProvider(
            privateKey = privateKey,
            signature = signature
        )
    }

    @Test
    fun `Wraps InvalidKeyExceptions in UnrecoverableError instances`() = runTest {
        val invalidKeyException = InvalidKeyException("This is a unit test")
        every {
            signature.initSign(privateKey)
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
        val exception = java.security.SignatureException("This is a unit test")
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

        privateKey = keyPair.private as ECPrivateKey
        signature = Signature.getInstance("SHA256withECDSA")

        val result = provider.sign(byteArrayOf(1, 2, 3, 4, 5))

        assertNotNull(result)
    }
}
