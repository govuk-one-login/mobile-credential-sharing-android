package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.testapp.verifier.auth.issuer.IssuerRootCertificateProvider
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.ReaderAuthCertificateValidator
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

    private val issuerRootCertificateProvider by lazy {
        IssuerRootCertificateProvider(
            ApplicationProvider.getApplicationContext()
        )
    }

    private val validator by lazy {
        ReaderAuthCertificateValidator(ApplicationProvider.getApplicationContext())
    }

    private val viewModel by lazy {
        SelectCredentialsViewModel(
            readerAuthFactory = factory,
            certificateValidator = validator,
            issuerRootCertificateProvider = issuerRootCertificateProvider
        )
    }

    /**
     * Options whose bundled leaf certificate is provisioned (valid) rather than
     * an unprovisioned DVS placeholder. Only these allow verification.
     */
    enum class ProvisionedReaderAuthOption(val option: ReaderAuthOption) {
        VALID(ReaderAuthOption.VALID),
        INVALID_NAME_CONSTRAINTS(ReaderAuthOption.INVALID_NAME_CONSTRAINTS),
        INVALID_MISSING_PRIVACY_POLICY(ReaderAuthOption.INVALID_MISSING_PRIVACY_POLICY),
        DVS_INTEGRATION(ReaderAuthOption.DVS_INTEGRATION),
        DVS_P256(ReaderAuthOption.DVS_P256),
        DVS_P384(ReaderAuthOption.DVS_P384),
        DVS_HYBRID(ReaderAuthOption.DVS_HYBRID),
        DVS_P384_LEAF_P256_CA(ReaderAuthOption.DVS_P384_LEAF_P256_CA),
        DVS_P384_LEAF_P256_CA_NO_POLICY(ReaderAuthOption.DVS_P384_LEAF_P256_CA_NO_POLICY),
        DVS_HYBRID_UNSUPPORTED(ReaderAuthOption.DVS_HYBRID_UNSUPPORTED),
        DVS_INVALID_CURVE(ReaderAuthOption.DVS_INVALID_CURVE)
    }

    /** DVS options that are unprovisioned placeholders in a non-pipeline build. */
    enum class PlaceholderReaderAuthOption(val option: ReaderAuthOption) {
        DVS_DEV(ReaderAuthOption.DVS_DEV)
    }

    @Test
    fun `Passes exact VerifierAttributeOption when tapping 'Verify credential' button`(
        @TestParameter option: VerifierAttributeOption
    ) = runTest {
        var selectedOption: VerifierAttributeOption? = null
        composeTestRule.run {
            setContent {
                SelectCredentialAttributesScreen(
                    onSelectAttributeGroup = { selectedOption = it },
                    viewModel = viewModel
                )
            }

            performAttributeGroupClick(option)
            assertOptionIsSelected(option)
            performVerifyCredentialClick()
            assertEquals(option, selectedOption)
        }
    }

    @Test
    fun `Provisioned reader auth options verify without a warning`(
        @TestParameter provisioned: ProvisionedReaderAuthOption
    ) = runTest {
        composeTestRule.run {
            setContent {
                SelectCredentialAttributesScreen(
                    onSelectAttributeGroup = {
                        composeTestRule.updateConfirmedAttributeGroup(it.attributeGroup)
                    },
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
                    onSelectAttributeGroup = { updateConfirmedAttributeGroup(it.attributeGroup) },
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

    @Test
    fun `Selects issuer root option before tapping 'Verify credential' button`(
        @TestParameter option: IssuerRootOption
    ) = runTest {
        composeTestRule.run {
            setContent {
                SelectCredentialAttributesScreen(
                    onSelectAttributeGroup = {
                        composeTestRule.updateConfirmedAttributeGroup(it.attributeGroup)
                    },
                    viewModel = viewModel
                )
            }

            performIssuerRootClick(option)
            assertOptionIsSelected(option)
            performVerifyCredentialClick()
        }
    }
}
