package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.consent.HolderConsentScreen
import uk.gov.onelogin.sharing.holder.consent.HolderConsentViewModel
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import kotlin.test.Test

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestParameterInjector::class)
class MoreAboutDvsScreenTest {

    @get:Rule
    val composeTestRule = MoreAboutDvsScreenRule()

    private var organizationName: String = "Stub-DVS Ltd."

    private val analyticsLogger = FakeAnalyticsLogger()

    private val presentation by lazy {
        createConsentPresentation(
            organizationName = organizationName
        )
    }

    private var initialHolderState: HolderSessionState =
        HolderSessionState.AwaitingUserConsent(
            request = mockk(relaxed = true),
            presentation = presentation
        )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        HolderConsentViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
            showMoreAboutDvs = MutableStateFlow(true)
        )
    }

    @Test
    fun `UI contains the correct elements`() = verifyUiElements(
        expectedOrganization = "Stub-DVS Ltd."
    ) {
        Render()
    }

    @Test
    fun `Invalid holder session states show a Progress indicator`() = runTest {
        initialHolderState = HolderSessionState.NotStarted

        composeTestRule.run {
            setContent { Render() }
            assertProgressIndicatorIsDisplayed()
        }
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
            onNode(
                hasParent(
                    hasTestTag("MoreAboutDvsContent")
                ) and hasTestTag("BODY_LAZY_COLUMN_TEST_TAG")
            ).performScrollToNode(
                hasTestTag("openUri")
            )
            performPrivacyPolicyButtonClick()
            waitForIdle()

            waitUntil("Doesn't contain expected event!: $analyticsLogger") {
                analyticsLogger.contains(MoreAboutDvsTestData.expectedPrivacyPolicyButtonEvent)
            }
        }
    }

    @Test
    fun `Opens a CustomTabsIntent when tapping the privacy policy button`() = runTest {
        Intents.init()

        composeTestRule.run {
            setContent { Render() }
            performPrivacyPolicyButtonClick()
            waitForIdle()
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(presentation.privacyPolicyUrl)
            )
        )

        Intents.release()
    }

    @Composable
    private fun Render() {
        GdsTheme {
            HolderConsentScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    private fun verifyUiElements(expectedOrganization: String, content: @Composable () -> Unit) =
        runTest {
            composeTestRule.run {
                organizationName = expectedOrganization
                setContent { GdsTheme { content() } }
                waitForIdle()

                assertValidUi(expectedOrganization = expectedOrganization)
            }
        }
}
