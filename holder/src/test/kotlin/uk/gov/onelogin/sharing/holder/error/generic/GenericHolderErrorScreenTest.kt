package uk.gov.onelogin.sharing.holder.error.generic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import kotlin.test.Test

@RunWith(RobolectricTestParameterInjector::class)
class GenericHolderErrorScreenTest {

    @get:Rule
    val composeTestRule = GenericHolderErrorScreenRule()

    private var initialHolderState: HolderSessionState = HolderSessionState.Complete.Failed(
        error = SessionError(
            "This is a unit test",
            SessionErrorReason.InvalidSessionDataPayload
        )
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
            orchestrator = orchestrator,
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
                analyticsLogger.contains(expectedScreenEvent)
            }
        }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            GenericHolderErrorScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
            )
        }
    }
}

