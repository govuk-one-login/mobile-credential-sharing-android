package uk.gov.onelogin.sharing.verifier.cancellation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@RunWith(AndroidJUnit4::class)
class VerifierCancellationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasResetJourney = false

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(VerifierSessionState.Complete.Cancelled)
        )
    }

    private val viewModel by lazy {
        VerifierCancellationScreenViewModel(
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Validate UI contents`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            onNodeWithTag("progressIndicator").assertExists()
        }
    }

    @Test
    fun `Calls 'onCancel' when launching the screen`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            waitUntil { hasResetJourney }
            assertThat(
                orchestrator.resetCount,
                equalTo(1)
            )
        }
    }

    @Composable
    private fun Render(
        content: @Composable () -> Unit = {
            VerifierCancellationScreen(
                onCancelJourney = { hasResetJourney = true },
                viewModel = viewModel
            )
        }
    ) {
        GdsTheme { content() }
    }
}
