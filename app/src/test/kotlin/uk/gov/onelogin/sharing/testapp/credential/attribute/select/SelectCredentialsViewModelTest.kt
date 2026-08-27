package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.TestAppReaderAuthCredentialProviderFactory

@RunWith(RobolectricTestParameterInjector::class)
class SelectCredentialsViewModelTest {

    private val factory = TestAppReaderAuthCredentialProviderFactory(
        ApplicationProvider.getApplicationContext()
    )

    private val viewModel by lazy {
        SelectCredentialsViewModel(
            readerAuthFactory = factory
        )
    }

    @Test
    fun `initial state is valid`() = runTest {
        viewModel.readerAuthOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(ReaderAuthOption.VALID)
            )
        }
    }

    @Test
    fun `Updates factory instance with reader auth option`(
        @TestParameter option: ReaderAuthOption
    ) = runTest {
        viewModel.update(option)

        viewModel.readerAuthOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(option)
            )
        }

        factory.readerAuthOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(option)
            )
        }
    }
}
