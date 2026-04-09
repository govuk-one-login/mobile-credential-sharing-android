package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.junit4.createComposeRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(RobolectricTestParameterInjector::class)
class VerifierPrerequisitesScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = VerifyCredentialRule(createComposeRule())

    private val logger = SystemLogger()

    private var initialSessionState: VerifierSessionState = VerifierSessionState.NotStarted

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialSessionState)
        )
    }
    private val viewModel by lazy {
        VerifierPrerequisitesViewModel(
            logger,
            orchestrator,
            dispatcherRule.testDispatcher
        )
    }

    @Test
    @TestParameters(valuesProvider = VerifierPrerequisitesScreenHandlers::class)
    fun `Calls lambdas based on VerifierSessionState`(
        state: VerifierSessionState,
        handlerAssertion: VerifyCredentialRule.() -> Unit
    ) = runTest(dispatcherRule.testDispatcher) {
        initialSessionState = state

        composeTestRule.run {
            composeTestRule.setContent {
                VerifierPrerequisitesScreen(
                    viewModel = viewModel,
                    onNavigateToPreflight = { this.updateHasNavigatedToPreflight() },
                    onNavigateToScanner = { this.updateHasNavigatedToScanner() },
                    onUnrecoverableError = { this.updateHasUnrecoverableError() }
                )
            }

            handlerAssertion(this)
        }
    }
}
