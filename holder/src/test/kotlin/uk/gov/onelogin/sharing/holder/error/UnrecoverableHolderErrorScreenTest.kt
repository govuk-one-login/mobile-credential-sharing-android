package uk.gov.onelogin.sharing.holder.error

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

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
    fun `Exiting the journey resets the orchestrator via the view model`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        composeTestRule.run {
            setContent {
                UnrecoverableHolderErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onExitJourney = { composeTestRule.updateHasExitedJourney() }
                )
            }

            onNodeWithRole(Role.Button).performClick()

            assertHasExitedJourney()
        }
    }
}
