package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(RobolectricTestParameterInjector::class)
class AwaitingVerifierResolutionScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = AwaitingVerifierResolutionScreenRule()

    private val orchestrator = FakeOrchestrator()
    private val analyticsLogger = StoredAnalyticsLogger()
    private val viewModel = AwaitingVerifierResolutionViewModel(analyticsLogger, orchestrator)
    private var exitJourneyCount = 0

    @Test
    fun `Displays the details shared content`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Screen" to { Render() },
            "Preview" to { RenderPreview() }
        )
    ) = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.setContent { content() }
        composeTestRule.run {
            assertSuccessIconIsDisplayed()
            assertTitleIsDisplayed()
            assertBodyIsDisplayed()
            assertGoBackToDocumentsButtonIsDisplayed()
        }
    }

    @Test
    fun `Go back to documents resets the journey and exits once`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.run {
                composeTestRule.setContent { Render() }
                performGoBackToDocumentsClick()
                waitUntil { orchestrator.dismissCount == 1 }
                waitUntil { exitJourneyCount == 1 }
            }
        }

    @Test
    fun `System back resets the journey and hands back press to the host app`() =
        runTest(dispatcherRule.testDispatcher) {
            var hostBackPresses = 0

            composeTestRule.run {
                var backPressedDispatcher: OnBackPressedDispatcherOwner? = null

                setContent {
                    backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                    BackHandler { hostBackPresses++ }
                    AwaitingVerifierResolutionScreen(
                        viewModel = viewModel,
                        onExitJourney = {
                            backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
                        }
                    )
                }

                runOnUiThread {
                    backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
                }
                waitForIdle()
                assertEquals(1, orchestrator.dismissCount, "Journey was not reset")
                assertEquals(1, hostBackPresses, "Host app did not press back")
            }
        }

    @Test
    fun `Exits only after the vertical down animation completes`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.run {
                setContent { Render() }
                mainClock.autoAdvance = false

                performGoBackToDocumentsClick()
                mainClock.advanceTimeByFrame()
                assertEquals(0, orchestrator.dismissCount)
                assertEquals(0, exitJourneyCount)

                mainClock.autoAdvance = true
                waitUntil { exitJourneyCount == 1 }
                assertEquals(1, orchestrator.dismissCount)
            }
        }

    @Test
    @Config(qualifiers = "cy")
    fun `Displaying the screen in Welsh sends analytics events in English`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.run {
                setContent { Render() }
                assertWelshTitleIsDisplayed()
                performGoBackToDocumentsClick()
                waitUntil { analyticsLogger.size >= 2 }
            }
            assertThat(
                analyticsLogger,
                hasSentEvents(
                    contains(
                        AwaitingVerifierResolutionTestData.expectedScreenEvent,
                        AwaitingVerifierResolutionTestData.expectedGoBackToDocumentsEvent
                    )
                )
            )
        }

    @Composable
    private fun Render() {
        AwaitingVerifierResolutionScreen(
            viewModel = viewModel,
            onExitJourney = { exitJourneyCount++ }
        )
    }

    @Composable
    private fun RenderPreview() {
        AwaitingVerifierResolutionScreenPreview()
    }
}
