package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.google.testing.junit.testparameterinjector.TestParameter
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ECReaderAuthProvider
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption

@RunWith(RobolectricTestParameterInjector::class)
class TestAppReaderAuthCredentialProviderFactoryTest(
    @TestParameter
    private val option: ReaderAuthOption
) {

    private var initialState: ReaderAuthOption = ReaderAuthOption.VALID

    private val logger = SystemLogger()

    private fun factory() = TestAppReaderAuthCredentialProviderFactory(
        ApplicationProvider.getApplicationContext(),
        logger = logger,
        initialState = initialState,
        keyFactory = KeyFactory.getInstance("EC"),
        certificateFactory = CertificateFactory.getInstance("X.509")
    )

    @Test
    fun `Initially selected option is configurable`() = runTest {
        initialState = option

        factory().readerAuthOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(option)
            )
        }
    }

    @Test
    fun `Internal state is updatable`() = runTest {
        val providerFactory = factory()
        providerFactory.update(option)

        providerFactory.readerAuthOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(option)
            )
        }
    }

    @Test
    fun `Creates ECReaderAuthProvider instances`() = runTest {
        initialState = option

        val result = factory().create()

        assertThat(
            result,
            instanceOf(ECReaderAuthProvider::class.java)
        )
    }

    @Test
    fun `Emitted x5chain excludes the root certificate`() = runTest {
        initialState = option

        val coseSign1 = factory().create().sign(byteArrayOf(1, 2, 3, 4, 5))

        // COSE_Sign1 = [protectedHeader, unprotectedHeader, null, signature].
        // The unprotected header is { 33: x5chain }. The asset chain is
        // [leaf, intermediate, root]; the root must be excluded, leaving 2 entries.
        val unprotectedHeader = ObjectMapper(CBORFactory()).readTree(coseSign1)[1]
        val x5chain = unprotectedHeader[X5CHAIN_LABEL]

        assertThat(x5chain.isArray, equalTo(true))
        assertThat(x5chain.size(), equalTo(EXPECTED_X5CHAIN_SIZE))
    }

    private companion object {
        const val X5CHAIN_LABEL = "33"
        const val EXPECTED_X5CHAIN_SIZE = 2
    }
}
