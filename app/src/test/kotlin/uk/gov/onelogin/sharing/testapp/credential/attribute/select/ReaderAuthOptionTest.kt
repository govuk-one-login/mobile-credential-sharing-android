package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import java.io.IOException
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class ReaderAuthOptionTest(@TestParameter val option: ReaderAuthOption) {
    val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `Provides a certificate chain list`() = runTest {
        val expected = listOf(
            "test_reader_auth_x509_certificate.der",
            "test_reader_auth_name_constrained_x509_certificate.der",
            "${option.leafCertificateAsset}.der"
        )

        assertThat(
            option.certificateChain,
            contains(expected)
        )
    }

    @Test
    fun `Provides a private key chain list`() = runTest {
        val expected = listOf(
            "test_reader_auth_x509_certificate.pem",
            "test_reader_auth_name_constrained_x509_certificate.pem",
            "${option.leafCertificateAsset}.pem"
        )

        assertThat(
            option.privateKeyChain,
            contains(expected)
        )
    }

    private fun openAsset(fileName: String) = try {
        context.assets.open(fileName)
    } catch (exception: IOException) {
        fail("Couldn't open '$fileName'", exception)
    }
}
