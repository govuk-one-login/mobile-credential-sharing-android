package uk.gov.onelogin.sharing.holder.error.generic

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedGoBackToDocumentsButtonEvent
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@RunWith(RobolectricTestParameterInjector::class)
class GenericHolderErrorScreenTest {

    @get:Rule
    val composeTestRule = GenericHolderErrorScreenRule()

    val error = SessionError(
        "This is a unit test",
        SessionErrorReason.InvalidSessionDataPayload
    )
    private var initialHolderState: HolderSessionState = HolderSessionState.Complete.Failed(
        error = error
    )

    private val analyticsLogger = FakeAnalyticsLogger()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        GenericHolderErrorViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Is valid UI`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Screen-level composable" to {
                Render()
            },
            "Preview composable" to {
                GdsTheme { GenericHolderErrorScreenPreview() }
            }
        )
    ) = runTest {
        composeTestRule.run {
            setContent(content)
            assertValidUi()
        }
    }

    @Test
    fun `Launching the screen sends an analytics event`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            waitForIdle()

            waitUntil("Cannot find expected event!: $analyticsLogger") {
                analyticsLogger.contains(expectedScreenEvent(error))
            }
        }
    }

    @Test
    fun `Resets the orchestrator when going back to documents`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            waitUntil("Orchestrator shouldn't have reset yet!") {
                orchestrator.resetCount == 0
            }

            performGoBackToDocumentsClick()

            waitUntil("Orchestrator should have been reset!") {
                orchestrator.resetCount == 1
            }
        }
    }

    @Test
    fun `Navigating back to documents also sends an analytics event`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            performGoBackToDocumentsClick()

            waitUntil("Cannot find expected event!: $analyticsLogger") {
                analyticsLogger.contains(expectedGoBackToDocumentsButtonEvent)
            }
        }
    }

    @Test
    fun `Pressing back on the device resets the orchestrator`() = runTest {
        composeTestRule.run {
            var onBackPressedDispatcher: OnBackPressedDispatcher? = null

            setContent {
                onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                    ?.onBackPressedDispatcher
                Render()
            }
            waitUntil("Orchestrator shouldn't have reset yet!") {
                orchestrator.resetCount == 0
            }

            runOnUiThread {
                onBackPressedDispatcher?.onBackPressed() ?: fail(
                    "Cannot find back press dispatcher!"
                )
            }

            waitUntil("Orchestrator should have been reset!") {
                orchestrator.resetCount == 1
            }
        }
    }

    @Test
    fun `Pressing back on the device sends the same event as going back to documents`() = runTest {
        var onBackPressedDispatcher: OnBackPressedDispatcher? = null
        composeTestRule.run {
            setContent {
                onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                    ?.onBackPressedDispatcher
                Render()
            }
            waitForIdle()

            runOnUiThread {
                onBackPressedDispatcher?.onBackPressed() ?: fail(
                    "Cannot find back press dispatcher!"
                )
            }

            waitUntil("Cannot find expected event!: $analyticsLogger") {
                analyticsLogger.contains(expectedGoBackToDocumentsButtonEvent)
            }
        }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            GenericHolderErrorScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )
        }
    }
}
