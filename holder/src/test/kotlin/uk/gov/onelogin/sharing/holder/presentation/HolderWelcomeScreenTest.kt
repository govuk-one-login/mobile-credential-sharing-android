package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
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

    private val holderState = MutableStateFlow(initialHolderState)

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        HolderWelcomeViewModel(
            dispatcher = dispatcherRule.testDispatcher,
            analyticsLogger = StoredAnalyticsLogger(),
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Shows QR code whilst presenting engagement`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent { Render() }
            assertQrCodeIsDisplayed()
        }
    }

    @Test
    fun `Shows a loading spinner before QR code is ready`() = runTest {
        initialHolderState = HolderSessionState.NotStarted
        composeTestRule.run {
            setContent { Render() }
            assertLoadingSpinnerIsDisplayed()
        }
    }

    @Test
    fun `Closing the journey shows a loading spinner`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            composeTestRule.assertHeadingIsDisplayed()
            composeTestRule.assertQrCodeIsDisplayed()
            composeTestRule.assertBodyIsDisplayed()
            holderState.update { HolderSessionState.SendingTermination }
            waitUntil {
                onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            assertLoadingSpinnerIsDisplayed()
        }
    }

    @Test
    fun `Preview only shows QR content`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent {}
            assertHeadingIsDisplayed()
            assertQrCodeIsDisplayed()
        }
    }

    @Composable
    fun Render() {
        HolderWelcomeScreen(viewModel = viewModel)
    }
}
