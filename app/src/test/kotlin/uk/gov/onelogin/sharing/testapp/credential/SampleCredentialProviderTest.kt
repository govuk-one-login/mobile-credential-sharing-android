package uk.gov.onelogin.sharing.testapp.credential

import androidx.test.core.app.ApplicationProvider
import java.security.Signature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialData.mockCredentialState

@RunWith(RobolectricTestRunner::class)
class SampleCredentialProviderTest {
    private val realCredential = mockCredentialState.toCredential(
        ApplicationProvider.getApplicationContext()
    )

    private val stubCredential = realCredential.copy(
        privateKey = SampleCredentialProviderStub.keyPair.private.encoded
    )

    private val credentialProvider by lazy {
        SampleCredentialProvider(stubCredential)
    }

    @Test
    fun `holds a single active credential on initialisation`() = runTest {
        val credentials =
            credentialProvider.getCredentials(CredentialRequest(documentTypes = emptyList()))
        assertEquals(1, credentials.size)
    }

    @Test
    fun `getCredentials returns active credential rawCredential`() = runTest {
        val credentials =
            credentialProvider.getCredentials(CredentialRequest(documentTypes = emptyList()))

        assertArrayEquals(realCredential.rawCredential, credentials.first().rawCredential)
    }

    @Test
    fun `sign returns a valid SHA256withECDSA signature for DeviceAuthentication bytes`() =
        runTest {
            val deviceAuthenticationBytes = SampleCredentialProviderStub.DEVICE_AUTHENTICATION_HEX
                .chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()

            val signature = credentialProvider.sign(
                payload = deviceAuthenticationBytes,
                documentId = "org.iso.18013.5.1.mDL"
            )

            val isValid = Signature.getInstance(SIGNING_ALGORITHM).run {
                initVerify(SampleCredentialProviderStub.keyPair.public)
                update(deviceAuthenticationBytes)
                verify(signature)
            }
            assertTrue(isValid)
        }

    @Test
    fun `sign produces different signatures for different payloads`() = runTest {
        val sig1 = credentialProvider.sign("payload-one".toByteArray(), documentId = "doc-id")
        val sig2 = credentialProvider.sign("payload-two".toByteArray(), documentId = "doc-id")
        assertTrue(!sig1.contentEquals(sig2))
    }
}
