package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.services.events.run.TestRunEvent
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(RobolectricTestParameterInjector::class)
class AwaitingVerifierResolutionScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = AwaitingVerifierResolutionScreenRule()

    private val orchestrator = FakeOrchestrator()
    private val viewModelFactory: MetroViewModelFactory = mockk(relaxed = true)
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
                waitUntil { orchestrator.resetCount == 1 }
                waitUntil { exitJourneyCount == 1 }
            }
        }

    @Test
    fun `System back is swallowed and does not exit the journey`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.run {
                var backPressedDispatcher: OnBackPressedDispatcherOwner? = null
                setContent {
                    backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                    Render()
                }
                runOnUiThread { backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed() }
                waitForIdle()
                assertEquals(0, orchestrator.resetCount)
                assertEquals(0, exitJourneyCount)
                assertTitleIsDisplayed()
            }
        }

    @Test
    fun `System back is handed to host app on go back to documents click`() =
        runTest(dispatcherRule.testDispatcher) {
            var hostBackPresses = 0

            composeTestRule.run {
                var backPressedDispatcher: OnBackPressedDispatcherOwner? = null

                setContent {
                    backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                    BackHandler { hostBackPresses++ }
                    Render()
                }

                runOnUiThread {
                    backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
                }
                waitForIdle()
                assertEquals(1, hostBackPresses, "Host app did not press back")
            }
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
