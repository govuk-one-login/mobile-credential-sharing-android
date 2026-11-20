package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(RobolectricTestParameterInjector::class)
class BleRestrictedScreenTest {

    @get:Rule(order = 0)
    val dispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val screenRule = BleRestrictedScreenRule()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(
                HolderSessionState.Preflight(missingPrerequisites = emptyList())
            )
        )
    }

    private val analyticsLogger by lazy { FakeAnalyticsLogger() }

    private val viewModel by lazy {
        BleRestrictedViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `UI contains the correct elements`() = verifyUiState { Render() }

    @Test
    fun `Preview function contains the correct elements`() = verifyUiState {
        BleRestrictedContentPreview()
    }

    private fun verifyUiState(content: @Composable () -> Unit) = runTest {
        screenRule.run {
            setContent { content() }
            assertValidUi()
        }
    }

    @Test
    fun `Displaying the screen sends a screen view event`() = runTest {
        screenRule.run {
            setContent { Render() }
            waitUntil { analyticsLogger.size != 0 }
        }
        assertThat(
            analyticsLogger,
            contains(BleRestrictedViewModelTestData.expectedScreenEvent)
        )
    }

    @Test
    fun `Tapping Go back to documents resets the journey and dismisses`() = runTest {
        var dismissed = false
        screenRule.run {
            setContent { Render(onGoBackToDocuments = { dismissed = true }) }
            waitUntil { analyticsLogger.size == 1 }

            performGoBackClick()

            waitUntil { dismissed }
            waitUntil { analyticsLogger.size == 2 }
        }

        assertThat(orchestrator.resetCount, equalTo(1))
        assertThat(
            analyticsLogger,
            contains(
                BleRestrictedViewModelTestData.expectedScreenEvent,
                BleRestrictedViewModelTestData.expectedGoBackEvent
            )
        )
    }

    @Test
    fun `Tapping Go back with default callback resets the journey without crashing`() = runTest {
        screenRule.run {
            setContent {
                GdsTheme {
                    BleRestrictedScreen(viewModel = viewModel)
                }
            }
            waitUntil { analyticsLogger.size == 1 }

            performGoBackClick()

            waitUntil { analyticsLogger.size == 2 }
            waitUntil { orchestrator.resetCount == 1 }
        }

        assertThat(orchestrator.resetCount, equalTo(1))
        assertThat(
            analyticsLogger,
            contains(
                BleRestrictedViewModelTestData.expectedScreenEvent,
                BleRestrictedViewModelTestData.expectedGoBackEvent
            )
        )
    }

    @Test
    fun `System back resets the journey and dismisses resets state`() = runTest {
        var dismissed = false
        var backPressedDispatcher: OnBackPressedDispatcherOwner? = null
        screenRule.run {
            setContent {
                backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                Render(onGoBackToDocuments = { dismissed = true })
            }
            waitUntil { analyticsLogger.size == 1 }

            runOnUiThread {
                backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
            }

            waitUntil("Didn't call `orchestrator.reset()` via the view model!") {
                orchestrator.resetCount == 1
            }
            waitUntil("Didn't call the 'onGoBackToDocuments' lambda!") { dismissed }
        }

        assertThat(dismissed, equalTo(true))
        assertThat(orchestrator.resetCount, equalTo(1))
    }

    @Test
    @Config(qualifiers = "cy")
    fun `Displaying the screen in Welsh sends analytics events in English`() = runTest {
        screenRule.run {
            setContent { Render() }

            assertWelshTitleIsDisplayed()
            waitUntil { analyticsLogger.size == 1 }

            performGoBackClick()
            waitUntil { analyticsLogger.size == 2 }
        }
        assertThat(
            analyticsLogger,
            contains(
                BleRestrictedViewModelTestData.expectedScreenEvent,
                BleRestrictedViewModelTestData.expectedGoBackEvent
            )
        )
    }

    @Composable
    private fun Render(onGoBackToDocuments: () -> Unit = {}) {
        GdsTheme {
            BleRestrictedScreen(
                viewModel = viewModel,
                onGoBackToDocuments = onGoBackToDocuments
            )
        }
    }
}
