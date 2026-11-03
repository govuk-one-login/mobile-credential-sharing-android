package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.performClick
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.analytics.logging.MemorisedAnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.analytics.SystemAnalyticsLogger
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import kotlin.test.Test

@RunWith(RobolectricTestParameterInjector::class)
class MoreAboutDvsScreenTest {

    @get:Rule
    val composeTestRule = MoreAboutDvsScreenRule()

    private var organizationName: String? = null

    private val analyticsLogger = FakeAnalyticsLogger()

    private val presentation by lazy {
        createConsentPresentation(
            organizationName = organizationName
        )
    }

    private val initialHolderState by lazy {
        HolderSessionState.AwaitingUserConsent(
            request = mockk(relaxed = true),
            presentation = presentation
        )
    }

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        MoreAboutDvsViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
        )
    }

    @Test
    fun `UI contains the correct elements`() = verifyUiElements(
        expectedOrganization = "Stub-DVS Ltd."
    ) {
        Render()
    }

    @Test
    fun `Preview contains the correct elements`() = verifyUiElements(
        expectedOrganization = "Stub-DVS Ltd."
    ) {
        MoreAboutDvsPreview(
            presentation = presentation
        )
    }

    @Test
    fun `Undefined organizations show 'Unknown' in the title`() = verifyUiElements(
        expectedOrganization = "Unknown"
    ) {
        organizationName = null
        Render()
    }

    @Test
    fun `Launching the screen sends an analytics event`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            waitUntil("Doesn't contain expected event!: $analyticsLogger") {
                analyticsLogger.contains(MoreAboutDvsTestData.expectedScreenEvent)
            }
        }
    }

    @Test
    fun `Reading the DVS privacy policy sends an analytics event`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            onPrivacyPolicyButton(true).performClick()

            waitUntil("Doesn't contain expected event!: $analyticsLogger") {
                analyticsLogger.contains(MoreAboutDvsTestData.expectedButtonEvent)
            }
        }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            MoreAboutDvsScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
            )
        }
    }

    private fun verifyUiElements(
        expectedOrganization: String,
        content: @Composable () -> Unit,
    ) = runTest {
        composeTestRule.run {
            organizationName = expectedOrganization
            setContent { GdsTheme { content() } }
            waitForIdle()

            assertValidUi(expectedOrganization = expectedOrganization)
        }
    }
}