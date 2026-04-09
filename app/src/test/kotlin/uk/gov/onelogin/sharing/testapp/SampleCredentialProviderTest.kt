package uk.gov.onelogin.sharing.testapp

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.security.Signature
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.CredentialRequest
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub.DEVICE_AUTHENTICATION_HEX
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub.base64EncodedCredential
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub.keyPair
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub.pkcs8PrivateKeyPem
import uk.gov.onelogin.sharing.testapp.SampleCredentialProviderStub.rawCredentialBytes

class SampleCredentialProviderTest {

    private fun buildContext(pemContent: String): Context = mockk {
        every { resources } returns mockk<Resources> {
            every { openRawResource(R.raw.mock_credential) } returns
                ByteArrayInputStream(base64EncodedCredential.toByteArray())
        }
        every { assets } returns mockk<AssetManager> {
            every { open("test_private_key.pem") } returns
                ByteArrayInputStream(pemContent.toByteArray())
        }
    }

    private val credentialProvider = SampleCredentialProvider(
        buildContext(pkcs8PrivateKeyPem)
    )

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
        assertArrayEquals(rawCredentialBytes, credentials.first().rawCredential)
    }

    @Test
    fun `getCredentials always returns exactly one credential regardless of documentTypes`() =
        runTest {
            val credentials = credentialProvider.getCredentials(
                CredentialRequest(
                    documentTypes = listOf("org.iso.18013.5.1.mDL", "other.doc.type")
                )
            )
            assertEquals(1, credentials.size)
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

            val isValid = Signature.getInstance("SHA256withECDSA").run {
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
