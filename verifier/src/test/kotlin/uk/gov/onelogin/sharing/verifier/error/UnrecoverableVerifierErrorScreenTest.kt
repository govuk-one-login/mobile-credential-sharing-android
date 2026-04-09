package uk.gov.onelogin.sharing.verifier.error

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
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@RunWith(AndroidJUnit4::class)
class UnrecoverableVerifierErrorScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = UnrecoverableVerifierErrorScreenRule(createComposeRule())

    private var initialVerifierState = VerifierSessionState.Complete.Failed(
        SessionError(
            "This is a unit test",
            SessionErrorReason.UnrecoverablePrerequisite()
        )
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialVerifierState),
            startCount = 1
        )
    }

    private val viewModel by lazy {
        UnrecoverableVerifierViewModel(
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
                UnrecoverableVerifierErrorScreen(
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
