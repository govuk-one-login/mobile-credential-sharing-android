package uk.gov.onelogin.sharing.holder.cancellation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(RobolectricTestParameterInjector::class)
class HolderCancellationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasResetJourney = false

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(HolderSessionState.Complete.Cancelled)
        )
    }

    private val viewModel by lazy {
        HolderCancellationScreenViewModel(orchestrator)
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
            HolderCancellationScreen(
                viewModel = viewModel,
                onCancelJourney = { hasResetJourney = true }
            )
        }
    ) {
        GdsTheme { content() }
    }
}
