package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderRootOption

@RunWith(AndroidJUnit4::class)
class ReaderRootCertificateProviderTest {

    private lateinit var context: Context
    private lateinit var provider: ReaderRootCertificateProvider

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        provider = ReaderRootCertificateProvider(context)
    }

    @Test
    fun `default option is SHARING_TEST_APP_MOCK`() = runTest {
        assertEquals(ReaderRootOption.SHARING_TEST_APP_MOCK, provider.readerRootOption.first())

        val certs = provider.trustedReaderCertificates()
        assertEquals(1, certs.size)

        val subjectName = certs.first().subjectX500Principal.name
        assertTrue(
            subjectName.contains("CN=Root") || subjectName.contains("CN=mDoc Test Issuer"),
            "Expected mock root certificate subject to contain CN=Root or CN=mDoc Test Issuer, got: $subjectName"
        )
    }

    @Test
    fun `update reader root option updates state flow`() = runTest {
        provider.update(ReaderRootOption.DVS_DEV)
        assertEquals(ReaderRootOption.DVS_DEV, provider.readerRootOption.first())

        provider.update(ReaderRootOption.DVS_INTEGRATION)
        assertEquals(ReaderRootOption.DVS_INTEGRATION, provider.readerRootOption.first())

        provider.update(ReaderRootOption.ALL)
        assertEquals(ReaderRootOption.ALL, provider.readerRootOption.first())
    }

    @Test
    fun `rootCertificateFor valid mock option returns certificate`() = runTest {
        val rootCert = provider.rootCertificateFor(ReaderAuthOption.VALID)
        assertNotNull(rootCert)
        val subjectName = rootCert.subjectX500Principal.name
        assertTrue(
            subjectName.contains("CN=Root") || subjectName.contains("CN=mDoc Test Issuer"),
            "Expected mock root certificate subject to contain CN=Root or CN=mDoc Test Issuer, got: $subjectName"
        )
    }
}
