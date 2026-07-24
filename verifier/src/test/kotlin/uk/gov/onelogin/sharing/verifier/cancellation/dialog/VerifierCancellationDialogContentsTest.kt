package uk.gov.onelogin.sharing.verifier.cancellation.dialog

import androidx.compose.runtime.Composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.cancellation.CancellationDialogContentsRule

@RunWith(AndroidJUnit4::class)
class VerifierCancellationDialogContentsTest {
    @get:Rule
    val composeTestRule = CancellationDialogContentsRule()

    private val orchestrator by lazy {
        FakeOrchestrator()
    }
    private val viewModel by lazy {
        VerifierCancellationDialogViewModel(orchestrator)
    }

    @Test
    fun `Validate UI contents`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            assertTitleIsDisplayed()
            assertCancelJourneyButtonIsDisplayed()
            assertDismissDialogButtonIsDisplayed()
        }
    }

    @Test
    fun `Dialog is dismissable`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            performDismissDialogClick()
            assertDismissDialogClickCount(1)
        }
    }

    @Test
    fun `Cancelling the journey defers to the orchestrator`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            performCancelJourneyClick()
            waitForIdle()

            assertThat(
                orchestrator.cancelCount,
                equalTo(1)
            )
        }
    }

    @Composable
    private fun Render(
        content: @Composable () -> Unit = {
            VerifierCancellationDialogContents(
                viewModel = viewModel,
                onDismiss = composeTestRule::incrementDismissDialogClickCount
            )
        }
    ) {
        GdsTheme { content() }
    }
}
