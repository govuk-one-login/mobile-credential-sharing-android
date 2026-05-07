package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(composeTestRule = createComposeRule())

    private var initialHolderState: HolderSessionState = HolderSessionState.PresentingEngagement(
        "This is a unit test"
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        HolderWelcomeViewModel(
            dispatcher = dispatcherRule.testDispatcher,
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Shows QR code whilst presenting engagement`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent { Render() }
            assertWelcomeTextIsDisplayed()
            assertQrCodeIsDisplayed()
        }
    }

    @Test
    fun `Preview only shows QR content`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent { HolderWelcomeScreenPreview() }
            assertWelcomeTextIsDisplayed()
            assertQrCodeIsDisplayed()
        }
    }

    @Composable
    fun Render() {
        HolderWelcomeScreen(viewModel = viewModel)
    }
}
