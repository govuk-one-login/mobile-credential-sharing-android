package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.TestAppReaderAuthCredentialProviderFactory

@RunWith(RobolectricTestParameterInjector::class)
class SelectCredentialAttributesScreenTest {

    @get:Rule
    val composeTestRule = SelectCredentialAttributesScreenRule(createComposeRule())

    private val logger = SystemLogger()

    private val factory by lazy {
        TestAppReaderAuthCredentialProviderFactory(
            ApplicationProvider.getApplicationContext(),
            logger
        )
    }

    private val viewModel by lazy {
        SelectCredentialsViewModel(readerAuthFactory = factory)
    }

    @Test
    fun `Attribute groups are passed to lambda when tapping 'Verify credential' button`(
        @TestParameter option: VerifierAttributeOption
    ) = runTest {
        composeTestRule.run {
            setContent {
                SelectCredentialAttributesScreen(
                    onSelectAttributeGroup = composeTestRule::updateConfirmedAttributeGroup,
                    viewModel = viewModel
                )
            }

            performAttributeGroupClick(option)
            assertOptionIsSelected(option)
            performVerifyCredentialClick()
            assertConfirmedAttributeGroupEquals(option.attributeGroup)
        }
    }

    @Test
    fun `Passes file name when tapping 'Verify credential' button`(
        @TestParameter option: ReaderAuthOption
    ) = runTest {
        composeTestRule.run {
            setContent {
                SelectCredentialAttributesScreen(
                    onSelectAttributeGroup = composeTestRule::updateConfirmedAttributeGroup,
                    viewModel = viewModel
                )
            }

            performReaderAuthClick(option)
            assertOptionIsSelected(option)
            performVerifyCredentialClick()
        }
    }
}
