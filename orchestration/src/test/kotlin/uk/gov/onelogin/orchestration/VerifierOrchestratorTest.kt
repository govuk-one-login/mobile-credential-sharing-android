package uk.gov.onelogin.orchestration

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.CANCEL_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers.hasCurrentState
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionImpl
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.session.verifier.data.CancellableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.session.verifier.data.UncancellableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.session.verifier.matchers.VerifierSessionStateMatchers.inPreflight
import uk.gov.onelogin.sharing.orchestration.session.verifier.matchers.VerifierSessionStateMatchers.isCancelled

@RunWith(TestParameterInjector::class)
class VerifierOrchestratorTest {
    private var initialState: VerifierSessionState = VerifierSessionState.NotStarted
    private val logger = SystemLogger()
    private val resetOrchestratorSessionLog = "Cleared Orchestrator verifier session"
    private val session by lazy {
        VerifierSessionImpl(
            logger = logger,
            internalState = MutableStateFlow(initialState)
        )
    }
    private val orchestrator by lazy {
        VerifierOrchestrator(
            logger = logger,
            session = session
        )
    }

    @Before
    fun setUp() {
        assertEquals(
            0,
            logger.size
        )
    }

    @Test
    fun `Starting the Orchestrator journey navigates to the Preflight state`() = runTest {
        orchestrator.start(setOf())

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            session,
            hasCurrentState(inPreflight())
        )
    }

    @Test
    fun `Orchestrator cannot be started more than once`() = runTest {
        `Starting the Orchestrator journey navigates to the Preflight state`()

        orchestrator.start(setOf())

        assert(START_ORCHESTRATION_ERROR in logger)
        assertThat(
            session,
            hasCurrentState(inPreflight())
        )
    }

    @Test
    fun `Orchestrator cannot cancel invalid state transitions`(
        @TestParameter(valuesProvider = UncancellableVerifierSessionStates::class)
        state: VerifierSessionState
    ) = runTest {
        initialState = state
        orchestrator.cancel()

        assert(CANCEL_ORCHESTRATION_ERROR in logger)
        assert(CANCEL_ORCHESTRATION_SUCCESS !in logger)
        assertThat(
            session,
            hasCurrentState(state)
        )
    }

    @Test
    fun `Cancelling the User journey is based on the internal session state`(
        @TestParameter(valuesProvider = CancellableVerifierSessionStates::class)
        state: VerifierSessionState
    ) = runTest {
        initialState = state
        orchestrator.cancel()

        assert(CANCEL_ORCHESTRATION_SUCCESS in logger)
        assert(CANCEL_ORCHESTRATION_ERROR !in logger)
        assertThat(
            session,
            hasCurrentState(isCancelled())
        )
    }

    @Test
    fun `Resetting the Orchestrator clears the VerifierSession`() = runTest {
        `Starting the Orchestrator journey navigates to the Preflight state`()

        orchestrator.reset()

        assert(resetOrchestratorSessionLog in logger)
        assertThat(
            session,
            hasCurrentState(VerifierSessionState.NotStarted)
        )
    }
}
