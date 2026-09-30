package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderRootOption

@RunWith(RobolectricTestParameterInjector::class)
class ReaderRootCertificateProviderTest {

    private val provider = ReaderRootCertificateProvider(
        ApplicationProvider.getApplicationContext()
    )

    @Test
    fun `Initial option is the sharing test app mock root`() = runTest {
        provider.readerRootOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(ReaderRootOption.SHARING_TEST_APP_MOCK)
            )
        }
    }

    @Test
    fun `Updates the selected reader root option`(@TestParameter option: ReaderRootOption) =
        runTest {
            provider.update(option)

            provider.readerRootOption.test {
                assertThat(
                    expectMostRecentItem(),
                    equalTo(option)
                )
            }
        }

    @Test
    fun `rootCertificateFor returns root certificate for mock option`() = runTest {
        val rootCert = provider.rootCertificateFor(ReaderAuthOption.VALID)

        assertNotNull(rootCert)
        assertEquals("X.509", rootCert.type)

        // Verify that the root cert is self-signed
        val isSelfSigned = try {
            rootCert.verify(rootCert.publicKey)
            true
        } catch (_: Exception) {
            false
        }
        assertTrue(isSelfSigned, "Root certificate should be self-signed")
    }

    @Test
    fun `trustedReaderCertificates returns root certificate for SHARING_TEST_APP_MOCK`() = runTest {
        provider.update(ReaderRootOption.SHARING_TEST_APP_MOCK)

        val certs = provider.trustedReaderCertificates()

        assertThat(certs.size, equalTo(1))
        assertEquals("X.509", certs.first().type)
    }
}
