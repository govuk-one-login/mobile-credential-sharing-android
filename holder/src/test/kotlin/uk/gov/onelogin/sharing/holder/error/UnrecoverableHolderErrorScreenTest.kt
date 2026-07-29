package uk.gov.onelogin.sharing.holder.error

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UnrecoverableHolderErrorScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = UnrecoverableHolderErrorScreenRule(createComposeRule())

    private var sessionState = HolderSessionState.Complete.Failed(
        SessionError(
            "This is a unit test",
            SessionErrorReason.UnrecoverablePrerequisite()
        )
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(sessionState),
            startCount = 1
        )
    }

    private val viewModel by lazy {
        UnrecoverableHolderViewModel(
            orchestrator = orchestrator,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Resets the orchestrator through the view model via CTA`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        composeTestRule.run {
            setContent { Render() }

            onNodeWithRole(Role.Button).performClick()

            assertHasExitedJourney()
            assertHasExitedJourneyCount(equalTo(1))
        }
    }

    @Test
    fun `Resets the orchestrator through the view model via back button press`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        composeTestRule.run {
            var backPressedDispatcher: OnBackPressedDispatcherOwner? = null
            setContent {
                backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                Render()
            }

            runOnUiThread {
                backPressedDispatcher!!.onBackPressedDispatcher.onBackPressed()
            }

            assertHasExitedJourney()
            assertHasExitedJourneyCount(equalTo(1))
        }
    }

    @Composable
    private fun Render() {
        UnrecoverableHolderErrorScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            onExitJourney = { composeTestRule.updateHasExitedJourney() }
        )
    }
}
