package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import androidx.test.core.app.ApplicationProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption

@RunWith(RobolectricTestParameterInjector::class)
class ReaderAuthCertificateValidatorTest {
    private val validator = ReaderAuthCertificateValidator(
        ApplicationProvider.getApplicationContext()
    )

    @Test
    fun `A real in-date leaf certificate is valid`() = runTest {
        assertEquals(
            ReaderAuthCertificateStatus.VALID,
            validator.validate(ReaderAuthOption.VALID)
        )
    }

    @Test
    fun `Placeholder sentinel bytes are treated as placeholder`() = runTest {
        assertEquals(
            ReaderAuthCertificateStatus.PLACEHOLDER,
            validator.classify("DVS_PLACEHOLDER_CERTIFICATE_REPLACE_IN_CI".encodeToByteArray())
        )
    }

    @Test
    fun `Non-certificate bytes are treated as placeholder`() = runTest {
        assertEquals(
            ReaderAuthCertificateStatus.PLACEHOLDER,
            validator.classify("this is not a certificate".encodeToByteArray())
        )
    }
}
