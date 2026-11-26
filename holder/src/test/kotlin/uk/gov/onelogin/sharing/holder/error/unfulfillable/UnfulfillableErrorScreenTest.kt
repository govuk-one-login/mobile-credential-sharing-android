package uk.gov.onelogin.sharing.holder.error.unfulfillable

import android.content.Context
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplateViewModel
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedGoBackToDocumentsButtonEvent
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorAnalyticsTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(RobolectricTestParameterInjector::class)
class UnfulfillableErrorScreenTest {

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val composeTestRule = UnfulfillableErrorScreenRule(
        resources = resources
    )

    private val contentTag = "content"
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
            orchestrator = orchestrator
        )
    }

    private val errorTemplateViewModel by lazy {
        ErrorScreenTemplateViewModel(
            analyticsLogger = analyticsLogger
        )
    }

    private val viewModelFactory: MetroViewModelFactory = TestViewModelFactory(
        viewModelProviders = mapOf(
            UnfulfillableErrorViewModel::class to { viewModel },
            ErrorScreenTemplateViewModel::class to { errorTemplateViewModel }
        )
    )

    @Test
    fun `Has valid UI elements`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Screen-level composable" to {
                Render()
            },
            "Preview composable" to {
                CompositionLocalProvider(
                    LocalMetroViewModelFactory provides viewModelFactory
                ) {
                    GdsTheme { UnfulfillableErrorScreenPreview() }
                }
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
                analyticsLogger.contains(expectedScreenEvent())
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

    @Test
    fun `Keyboard navigation accesses the bottom CTA`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            onNodeWithTag(contentTag).performKeyInput {
                pressKey(Key.DirectionDown)
                pressKey(Key.DirectionDown)
            }
            waitForIdle()

            assertButtonIsFocused()
        }
    }

    @Composable
    private fun Render() {
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            GdsTheme {
                UnfulfillableErrorScreen(
                    modifier = Modifier.fillMaxSize().testTag(contentTag)
                )
            }
        }
    }
}
