package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplateViewModel
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedGoBackToDocumentsButtonEvent
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorViewModel
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorAnalyticsTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import kotlin.test.Test
import kotlin.test.fail

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

    private val errorTemplateViewModel by lazy {
        ErrorScreenTemplateViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator
        )
    }

    private val viewModelFactory: MetroViewModelFactory = TestViewModelFactory(
        viewModelProviders = mapOf(
            UnfulfillableErrorViewModel::class to { viewModel },
            ErrorScreenTemplateViewModel::class to { errorTemplateViewModel }
        )
    )

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
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            GdsTheme {
                UnfulfillableErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}