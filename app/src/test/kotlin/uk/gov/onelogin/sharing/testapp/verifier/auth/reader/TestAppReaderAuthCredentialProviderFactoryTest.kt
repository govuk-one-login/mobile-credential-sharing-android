package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ECReaderAuthProvider
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption

@RunWith(RobolectricTestParameterInjector::class)
class TestAppReaderAuthCredentialProviderFactoryTest(
    @TestParameter
    private val option: ReaderAuthOption
) {

    private var initialState: ReaderAuthOption = ReaderAuthOption.VALID

    private val factory by lazy {
        TestAppReaderAuthCredentialProviderFactory(
            ApplicationProvider.getApplicationContext(),
            initialState = initialState
        )
    }

    @Test
    fun `Initially selected option is configurable`() = runTest {
        initialState = option

        factory.readerAuthOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(option)
            )
        }
    }

    @Test
    fun `Internal state is updatable`() = runTest {
        factory.update(option)

        factory.readerAuthOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(option)
            )
        }
    }

    @Test
    fun `Creates ECReaderAuthProvider instances`() = runTest {
        initialState = option

        val result = factory.create()

        assertThat(
            result,
            instanceOf(ECReaderAuthProvider::class.java)
        )
    }
}
