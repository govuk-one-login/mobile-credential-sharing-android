package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import androidx.test.core.app.ApplicationProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption

@RunWith(RobolectricTestParameterInjector::class)
class DvsP256Test {

    @Test
    fun `DVS_P256 certificate and root are parseable and valid`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val validator = ReaderAuthCertificateValidator(context)

        val statusP256 = validator.validate(ReaderAuthOption.DVS_P256)
        assertEquals(ReaderAuthCertificateStatus.VALID, statusP256)

        val statusP384 = validator.validate(ReaderAuthOption.DVS_P384)
        assertEquals(ReaderAuthCertificateStatus.VALID, statusP384)

        val rootProvider = ReaderRootCertificateProvider(context)
        val p256Root = rootProvider.rootCertificateFor(ReaderAuthOption.DVS_P256)
        assertNotNull(p256Root)

        val p384Root = rootProvider.rootCertificateFor(ReaderAuthOption.DVS_P384)
        assertNotNull(p384Root)
    }
}
