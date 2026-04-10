package uk.gov.onelogin.sharing.testapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.security.Signature
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub.DEVICE_AUTHENTICATION_HEX
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub.keyPair

@RunWith(RobolectricTestRunner::class)
class SampleCredentialProviderTest {
    private val context: Context by lazy { ApplicationProvider.getApplicationContext() }
    private val credentialProvider by lazy {
        val realCredential = MockCredentials.mockCredential(context)

        val stubCredential = MockCredential(
            id = realCredential.id,
            displayName = realCredential.displayName,
            rawCredential = realCredential.rawCredential,
            privateKey = keyPair.private.encoded
        )

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

        val realCredential = MockCredentials.mockCredential(context)
        assertArrayEquals(realCredential.rawCredential, credentials.first().rawCredential)
    }

    @Test
    fun `sign returns a valid SHA256withECDSA signature for DeviceAuthentication bytes`() =
        runTest {
            val deviceAuthenticationBytes = DEVICE_AUTHENTICATION_HEX
                .chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()

            val signature = credentialProvider.sign(
                payload = deviceAuthenticationBytes,
                documentId = "org.iso.18013.5.1.mDL"
            )

            val isValid = Signature.getInstance(SIGNING_ALGORITHM).run {
                initVerify(keyPair.public)
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
