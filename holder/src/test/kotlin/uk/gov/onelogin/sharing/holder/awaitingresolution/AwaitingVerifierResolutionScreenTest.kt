package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.compose.runtime.Composable
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(RobolectricTestParameterInjector::class)
class AwaitingVerifierResolutionScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = AwaitingVerifierResolutionScreenRule()

    private val orchestrator = FakeOrchestrator()
    private val viewModel = AwaitingVerifierResolutionViewModel(orchestrator)
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
