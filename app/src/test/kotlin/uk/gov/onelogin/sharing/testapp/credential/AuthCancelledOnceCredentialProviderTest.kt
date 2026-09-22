package uk.gov.onelogin.sharing.testapp.credential

import androidx.test.core.app.ApplicationProvider
import java.security.Signature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
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
    fun `first sign throws LocalAuthCancelled`() = runTest {
        assertFailsWith<MockSignException.LocalAuthCancelled> {
            credentialProvider.sign("payload".toByteArray(), documentId = "doc-id")
        }
    }

    @Test
    fun `second sign returns a valid signature`() = runTest {
        val payload = "device-authentication".toByteArray()

        assertFailsWith<MockSignException.LocalAuthCancelled> {
            credentialProvider.sign(payload, documentId = "doc-id")
        }

        val signature = credentialProvider.sign(payload, documentId = "doc-id")

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

        assertFailsWith<MockSignException.LocalAuthCancelled> {
            credentialProvider.sign(payload, documentId = "doc-id")
        }

        val sig1 = credentialProvider.sign(payload, documentId = "doc-id")
        val sig2 = credentialProvider.sign(payload, documentId = "doc-id")

        assertTrue(sig1.isNotEmpty())
        assertTrue(sig2.isNotEmpty())
    }
}
