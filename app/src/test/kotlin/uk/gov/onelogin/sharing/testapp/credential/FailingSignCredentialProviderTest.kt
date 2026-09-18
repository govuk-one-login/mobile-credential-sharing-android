package uk.gov.onelogin.sharing.testapp.credential

import androidx.test.core.app.ApplicationProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialData.mockCredentialState

@RunWith(RobolectricTestRunner::class)
class FailingSignCredentialProviderTest {
    private val realCredential = mockCredentialState.toCredential(
        ApplicationProvider.getApplicationContext()
    )

    private val stubCredential = realCredential.copy(
        privateKey = SampleCredentialProviderStub.keyPair.private.encoded
    )

    private val credentialProvider = FailingSignCredentialProvider(stubCredential)

    @Test
    fun `getCredentials returns the single active credential`() = runTest {
        val credentials =
            credentialProvider.getCredentials(CredentialRequest(documentTypes = emptyList()))

        assertEquals(1, credentials.size)
        assertArrayEquals(realCredential.rawCredential, credentials.first().rawCredential)
    }

    @Test
    fun `sign always throws SignError`() = runTest {
        assertFailsWith<MockSignException.SignError> {
            credentialProvider.sign("payload".toByteArray(), documentId = "doc-id")
        }
    }

    @Test
    fun `sign keeps failing on repeated attempts`() = runTest {
        repeat(3) {
            assertFailsWith<MockSignException.SignError> {
                credentialProvider.sign("payload".toByteArray(), documentId = "doc-id")
            }
        }
    }
}
