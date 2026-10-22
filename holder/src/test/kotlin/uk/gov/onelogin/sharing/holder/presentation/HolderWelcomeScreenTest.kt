package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(composeTestRule = createComposeRule())

    private var initialHolderState: HolderSessionState = HolderSessionState.PresentingEngagement(
        "This is a unit test"
    )

    private val holderState by lazy { MutableStateFlow(initialHolderState) }

    private val orchestrator by lazy {
        FakeOrchestrator(initialHolderState = holderState)
    }

    private val analyticsLogger = StoredAnalyticsLogger()

    private val viewModel by lazy {
        HolderWelcomeViewModel(
            dispatcher = dispatcherRule.testDispatcher,
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Shows QR code whilst presenting engagement`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent { Render() }
            assertHeadingIsDisplayed()
            assertQrCodeIsDisplayed()
            scrollToAndAssertBodyIsDisplayed()
        }
    }

    @Test
    fun `Shows a loading spinner before QR code is ready`() =
        runTest(dispatcherRule.testDispatcher) {
            initialHolderState = HolderSessionState.NotStarted
            composeTestRule.run {
                setContent { Render() }
                assertLoadingSpinnerIsDisplayed()
                assertHeadingDoesNotExist()
                assertQrCodeDoesNotExist()
                assertBodyDoesNotExist()
            }
        }

    @Test
    fun `Closing the journey shows a loading spinner`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent { Render() }
            assertHeadingIsDisplayed()
            assertQrCodeIsDisplayed()
            scrollToAndAssertBodyIsDisplayed()
            holderState.update { HolderSessionState.SendingTermination }
            waitUntilLoadingSpinnerExists()
            assertLoadingSpinnerIsDisplayed()
            assertHeadingDoesNotExist()
            assertQrCodeDoesNotExist()
            assertBodyDoesNotExist()
        }
    }

    @Test
    fun `Uses portrait content layout when in portrait orientation`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.run {
                setContent { Render() }
                assertQrCodeScrollsWithContent()
            }
        }

    @Test
    @Config(qualifiers = "+land")
    fun `Uses landscape content layout when in landscape orientation`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.run {
                setContent { Render() }
                assertQrCodeIsFixed()
                assertQrCodeSizeIs(250.dp)
                assertHeadingAndBodyAreScrollable()
            }
        }

    @Test
    fun `Displaying the screen sends a screen view event`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.run {
                setContent { Render() }
                waitUntil { analyticsLogger.sentAnalyticsEvents.isNotEmpty() }
            }
            assertThat(
                analyticsLogger,
                hasSentEvents(
                    contains(HolderWelcomeViewModelTestData.expectedScreenEvent)
                )
            )
        }

    @Test
    fun `Preview only shows QR content`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent { Render() }
            assertQrCodeIsDisplayed()
        }
    }

    @Composable
    fun Render() {
        HolderWelcomeScreen(viewModel = viewModel)
    }
}
