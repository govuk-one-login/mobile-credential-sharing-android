package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class ReaderAuthOptionTest(@TestParameter val option: ReaderAuthOption) {
    @Test
    fun `Provides a certificate chain list`() = runTest {
        val expected = listOf(
            "test_reader_auth_x509_certificate.der",
            "test_reader_auth_name_constrained_x509_certificate.der",
            "${option.leafCertificateAsset}.der"
        )

        assertEquals(
            expected,
            option.certificateChain
        )
    }

    @Test
    fun `Provides a private key chain list`() = runTest {
        val expected = listOf(
            "test_reader_auth_x509_certificate.pem",
            "test_reader_auth_name_constrained_x509_certificate.pem",
            "${option.leafCertificateAsset}.pem"
        )

        assertEquals(
            expected,
            option.privateKeyChain
        )
    }
}
