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
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.TestAppReaderAuthCredentialProviderFactory

@RunWith(RobolectricTestParameterInjector::class)
class SelectCredentialsViewModelTest {

    private val logger = SystemLogger()
    private val factory = TestAppReaderAuthCredentialProviderFactory(
        ApplicationProvider.getApplicationContext(),
        logger = logger
    )

    private val viewModel by lazy {
        SelectCredentialsViewModel(
            readerAuthFactory = factory
        )
    }

    @Test
    fun `initial reader auth option is valid`() = runTest {
        viewModel.readerAuthOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(ReaderAuthOption.VALID)
            )
        }
    }

    @Test
    fun `initial verifier attribute option is age over 21`() = runTest {
        viewModel.verifierAttributeOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(VerifierAttributeOption.PORTRAIT_AND_AGE_OVER_21)
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

    @Test
    fun `Updates internal state flow with verifier attribute option`(
        @TestParameter option: VerifierAttributeOption
    ) = runTest {
        viewModel.update(option)

        viewModel.verifierAttributeOption.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(option)
            )
        }
    }
}
