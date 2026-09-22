package uk.gov.onelogin.sharing.testapp.credential

import androidx.test.core.app.ApplicationProvider
import java.security.Signature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
import uk.gov.onelogin.sharing.orchestration.CredentialSigningException
import uk.gov.onelogin.sharing.orchestration.SignResult
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialData.mockCredentialState

@RunWith(RobolectricTestRunner::class)
class AuthCancelledOnceCredentialProviderTest {
    private val realCredential = mockCredentialState.toCredential(
        ApplicationProvider.getApplicationContext()
    )

    private val stubCredential = realCredential.copy(
        privateKey = SampleCredentialProviderStub.keyPair.private.encoded
    )

    private val credentialProvider = AuthCancelledOnceCredentialProvider(stubCredential)

    @Test
    fun `getCredentials returns the single active credential`() = runTest {
        val credentials =
            credentialProvider.getCredentials(CredentialRequest(documentTypes = emptyList()))

        assertEquals(1, credentials.size)
        assertArrayEquals(realCredential.rawCredential, credentials.first().rawCredential)
    }

    @Test
    fun `first sign returns a recoverable Failure mapped from the mock error`() = runTest {
        val result = credentialProvider.sign("payload".toByteArray(), documentId = "doc-id")

        val failure = assertIs<SignResult.Failure>(result)
        val exception = assertIs<CredentialSigningException.Recoverable>(failure.exception)
        assertTrue(exception.cause is MockSignException.LocalAuthCancelled)
    }

    @Test
    fun `second sign returns a valid signature`() = runTest {
        val payload = "device-authentication".toByteArray()

        assertIs<SignResult.Failure>(credentialProvider.sign(payload, documentId = "doc-id"))

        val result = credentialProvider.sign(payload, documentId = "doc-id")
        val signature = assertIs<SignResult.Success>(result).signature

        val isValid = Signature.getInstance(SIGNING_ALGORITHM).run {
            initVerify(SampleCredentialProviderStub.keyPair.public)
            update(payload)
            verify(signature)
        }
        assertTrue(isValid)
    }

    @Test
    fun `subsequent signs continue to succeed`() = runTest {
        val payload = "payload".toByteArray()

        assertIs<SignResult.Failure>(credentialProvider.sign(payload, documentId = "doc-id"))

        val sig1 = assertIs<SignResult.Success>(
            credentialProvider.sign(payload, documentId = "doc-id")
        ).signature
        val sig2 = assertIs<SignResult.Success>(
            credentialProvider.sign(payload, documentId = "doc-id")
        ).signature

        assertTrue(sig1.isNotEmpty())
        assertTrue(sig2.isNotEmpty())
    }
}
