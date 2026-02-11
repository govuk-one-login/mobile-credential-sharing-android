package uk.gov.onelogin.orchestration

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.CANCEL_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionImpl
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.holder.data.CancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.session.holder.data.UncancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.session.holder.matchers.HolderSessionStateMatchers.inPreflight
import uk.gov.onelogin.sharing.orchestration.session.holder.matchers.HolderSessionStateMatchers.isCancelled
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers.hasCurrentState

@RunWith(TestParameterInjector::class)
class HolderOrchestratorTest {
    private val logger = SystemLogger()
    private val resetOrchestratorSessionLog = "Cleared Orchestrator holder session"

    private var initialState: HolderSessionState = HolderSessionState.NotStarted

    private val session by lazy {
        HolderSessionImpl(
            logger = logger,
            internalState = MutableStateFlow(initialState)
        )
    }

    private val orchestrator by lazy {
        HolderOrchestrator(
            logger = logger,
            session = session
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
        @TestParameter(valuesProvider = UncancellableHolderSessionStates::class)
        state: HolderSessionState
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
        @TestParameter(valuesProvider = CancellableHolderSessionStates::class)
        state: HolderSessionState
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
    fun `Resetting the Orchestrator clears the HolderSession`() = runTest {
        `Starting the Orchestrator journey navigates to the Preflight state`()

        orchestrator.reset()

        assert(resetOrchestratorSessionLog in logger)
        assertThat(
            session,
            hasCurrentState(HolderSessionState.NotStarted)
        )
    }
}
