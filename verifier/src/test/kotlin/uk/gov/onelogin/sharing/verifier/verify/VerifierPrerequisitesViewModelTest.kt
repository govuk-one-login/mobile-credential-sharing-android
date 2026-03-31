package uk.gov.onelogin.sharing.verifier.verify

import app.cash.turbine.test
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.preflightEmptyPermissions

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPermissionsApi::class)
class VerifierPrerequisitesViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val logger = SystemLogger()
    private var sessionState: VerifierSessionState = VerifierSessionState.NotStarted

    private val fakeOrchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(sessionState)
        )
    }
    private val viewModel by lazy {
        VerifierPrerequisitesViewModel(
            logger = logger,
            orchestrator = fakeOrchestrator,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `orchestrator calls start on init`() = runTest(dispatcherRule.testDispatcher) {
        backgroundScope.launch { viewModel.events.collect { } }
        assertEquals(1, fakeOrchestrator.startCount)
    }

    @Test
    fun `Emits NavigateToScanner event due to orchestrator session state`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        sessionState = VerifierSessionState.ReadyToScan

        viewModel.events.test {
            assertEquals(
                VerifyCredentialEvents.NavigateToScanner,
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `Emits NavigateToPreflight event due to orchestrator`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        sessionState = preflightEmptyPermissions

        viewModel.events.test {
            assertEquals(
                VerifyCredentialEvents.NavigateToPreflight,
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }
}
