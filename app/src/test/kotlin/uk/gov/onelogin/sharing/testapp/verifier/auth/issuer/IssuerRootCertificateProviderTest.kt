package uk.gov.onelogin.sharing.testapp.verifier.auth.issuer

import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.IssuerRootOption

@RunWith(RobolectricTestParameterInjector::class)
class IssuerRootCertificateProviderTest {

    private val provider = IssuerRootCertificateProvider(
        ApplicationProvider.getApplicationContext()
    )

    @Test
    fun `Initial option is the sharing test app mock root`() = runTest {
        provider.issuerRootOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(IssuerRootOption.SHARING_TEST_APP_MOCK)
            )
        }
    }

    @Test
    fun `Updates the selected issuer root option`(@TestParameter option: IssuerRootOption) =
        runTest {
            provider.update(option)

            provider.issuerRootOption.test {
                assertThat(
                    expectMostRecentItem(),
                    equalTo(option)
                )
            }
        }

    @Test
    fun `Resolves a valid X509 certificate for each selectable option`(
        @TestParameter option: IssuerRootOption
    ) = runTest {
        provider.update(option)

        val certificate = provider.trustedRootCertificate()

        assertNotNull(certificate)
        assertEquals("X.509", certificate.type)
    }
}
