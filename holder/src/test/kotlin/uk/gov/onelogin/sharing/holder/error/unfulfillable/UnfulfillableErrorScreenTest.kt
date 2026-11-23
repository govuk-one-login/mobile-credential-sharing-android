package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorAnalyticsTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class UnfulfillableErrorScreenTest {
    @get:Rule
    val composeTestRule = UnfulfillableErrorScreenRule()

    private val analyticsLogger = FakeAnalyticsLogger()

    private val initialHolderSessionState: HolderSessionState = HolderSessionState.Complete.Success(
        HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderSessionState)
        )
    }

    private val viewModel by lazy {
        UnfulfillableErrorViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
        )
    }

    @Test
    fun `Has valid UI elements`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            assertValidUi()
        }
    }

    @Test
    fun `Launching the screen sends an analytics event`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            waitForIdle()

            waitUntil("Cannot find expected event!: $analyticsLogger") {
                analyticsLogger.contains(expectedScreenEvent())
            }
        }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            UnfulfillableErrorScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
            )
        }
    }
}