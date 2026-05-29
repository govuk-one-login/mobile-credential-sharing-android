package uk.gov.onelogin.sharing.verifier.error

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError

@RunWith(RobolectricTestParameterInjector::class)
class UnrecoverableVerifierErrorScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = UnrecoverableVerifierErrorScreenRule(createComposeRule())

    private var sessionError: SessionError =
        SessionError(
            "This is a unit test",
            SessionErrorReason.UnrecoverablePrerequisite()
        )

    private val initialVerifierState by lazy {
        VerifierSessionState.Complete.Failed(
            sessionError
        )
    }

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

    /**
     * DCMAW-20270: AC10: The Failed screen displays the [VerificationError] reason.
     */
    @Test
    fun `Unverifiable documents show the error to the User`(
        @TestParameter error: VerificationError
    ) {
        sessionError = SessionError(
            "Failed to verify provided documents (${error})",
            SessionErrorReason.UnverifiableDocument(error)
        )

        composeTestRule.run {
            setContent {
                UnrecoverableVerifierErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onExitJourney = { composeTestRule.updateHasExitedJourney() }
                )
            }

            onNodeWithText(sessionError.message)
                .assertExists()
                .assertIsDisplayed()
        }
    }
}
