package uk.gov.onelogin.sharing.holder.error.readerauth

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplateViewModel
import uk.gov.onelogin.sharing.holder.error.readerauth.ReaderAuthErrorScreenTestData.expectedGoBackToDocumentsButtonEvent
import uk.gov.onelogin.sharing.holder.error.readerauth.ReaderAuthErrorScreenTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@RunWith(RobolectricTestParameterInjector::class)
class ReaderAuthErrorScreenTest {

    @get:Rule
    val composeTestRule = ReaderAuthErrorScreenRule()

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
        ReaderAuthErrorViewModel(
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
            ReaderAuthErrorViewModel::class to { viewModel },
            ErrorScreenTemplateViewModel::class to { errorTemplateViewModel }
        )
    )

    @Test
    fun `Is valid UI`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Screen-level composable" to {
                Render()
            },
            "Preview composable" to {
                CompositionLocalProvider(
                    LocalMetroViewModelFactory provides viewModelFactory
                ) {
                    GdsTheme { ReaderAuthErrorScreenPreview() }
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
                analyticsLogger.contains(expectedScreenEvent(error))
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
    fun `Displaying the screen in English sends analytics events in English`() = runTest {
        assertAnalyticsEventsAreSentInEnglish()
    }

    @Test
    @Config(qualifiers = "cy")
    fun `Displaying the screen in Welsh sends analytics events in English`() = runTest {
        assertAnalyticsEventsAreSentInEnglish()
    }

    private fun assertAnalyticsEventsAreSentInEnglish() {
        composeTestRule.run {
            setContent { Render() }

            performGoBackToDocumentsClick()
            waitUntil { analyticsLogger.size >= 2 }
        }

        assertThat(
            analyticsLogger,
            contains(
                expectedScreenEvent(error),
                expectedGoBackToDocumentsButtonEvent
            )
        )
    }

    @Composable
    private fun Render() {
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            GdsTheme {
                ReaderAuthErrorScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
