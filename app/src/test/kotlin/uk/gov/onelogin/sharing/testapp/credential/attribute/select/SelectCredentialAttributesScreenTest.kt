package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.ReaderAuthCertificateValidator
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.TestAppReaderAuthCredentialProviderFactory

@RunWith(RobolectricTestParameterInjector::class)
class SelectCredentialAttributesScreenTest {

    @get:Rule
    val composeTestRule = SelectCredentialAttributesScreenRule(createComposeRule())

    private val factory by lazy {
        TestAppReaderAuthCredentialProviderFactory(
            ApplicationProvider.getApplicationContext()
        )
    }

    private val validator by lazy {
        ReaderAuthCertificateValidator(ApplicationProvider.getApplicationContext())
    }

    private val viewModel by lazy {
        SelectCredentialsViewModel(
            readerAuthFactory = factory,
            certificateValidator = validator
        )
    }

    /**
     * Options whose bundled leaf certificate is provisioned (valid) rather than
     * an unprovisioned DVS placeholder. Only these allow verification.
     */
    enum class ProvisionedReaderAuthOption(val option: ReaderAuthOption) {
        VALID(ReaderAuthOption.VALID),
        INVALID_NAME_CONSTRAINTS(ReaderAuthOption.INVALID_NAME_CONSTRAINTS),
        INVALID_MISSING_PRIVACY_POLICY(ReaderAuthOption.INVALID_MISSING_PRIVACY_POLICY)
    }

    /** DVS options that are unprovisioned placeholders in a non-pipeline build. */
    enum class PlaceholderReaderAuthOption(val option: ReaderAuthOption) {
        DVS_DEV(ReaderAuthOption.DVS_DEV),
        DVS_INTEGRATION(ReaderAuthOption.DVS_INTEGRATION)
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
    fun `Provisioned reader auth options verify without a warning`(
        @TestParameter provisioned: ProvisionedReaderAuthOption
    ) = runTest {
        composeTestRule.run {
            setContent {
                SelectCredentialAttributesScreen(
                    onSelectAttributeGroup = composeTestRule::updateConfirmedAttributeGroup,
                    viewModel = viewModel
                )
            }

            performReaderAuthClick(provisioned.option)
            assertOptionIsSelected(provisioned.option)
            performVerifyCredentialClick()
            assertNotProvisionedWarningNotShown()
        }
    }

    @Test
    fun `Unprovisioned DVS options warn when verification is attempted`(
        @TestParameter placeholder: PlaceholderReaderAuthOption
    ) = runTest {
        composeTestRule.run {
            setContent {
                SelectCredentialAttributesScreen(
                    onSelectAttributeGroup = composeTestRule::updateConfirmedAttributeGroup,
                    viewModel = viewModel
                )
            }

            performReaderAuthClick(placeholder.option)
            assertOptionIsSelected(placeholder.option)
            assertNotProvisionedWarningNotShown()
            performVerifyCredentialClick()
            assertNotProvisionedWarningShown()
        }
    }
}
