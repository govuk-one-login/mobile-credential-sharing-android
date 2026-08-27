package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class ReaderAuthOptionTest(@TestParameter val option: ReaderAuthOption) {
    val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `Certificate file exists`() = runTest {
        openAsset(option.leafCertificateAssetFileName)
    }

    @Test
    fun `Private key file exists`() = runTest {
        openAsset(option.leafCertificatePrivateKeyFileName)
    }

    @Test
    fun `Provides a certificate chain list`() = runTest {
        val expected = listOf(
            "test_reader_auth_x509_certificate",
            "test_reader_auth_name_constrained_x509_certificate",
            option.leafCertificateAsset
        )

        assertEquals(
            expected,
            option.certificateAssetChain
        )
    }

    private fun openAsset(fileName: String) = try {
        context.assets.open(fileName)
    } catch (exception: IOException) {
        fail("Couldn't open '$fileName'", exception)
    }
}
