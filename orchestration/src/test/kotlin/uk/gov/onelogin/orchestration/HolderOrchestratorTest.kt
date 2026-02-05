package uk.gov.onelogin.orchestration

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionImpl
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.holder.data.CancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.session.holder.data.UncancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.session.holder.matchers.HolderSessionMatchers.hasCurrentState
import uk.gov.onelogin.sharing.orchestration.session.holder.matchers.HolderSessionMatchers.inPreflight
import uk.gov.onelogin.sharing.orchestration.session.holder.matchers.HolderSessionStateMatchers.isCancelled

@RunWith(TestParameterInjector::class)
class HolderOrchestratorTest {
    private val logger = SystemLogger()
    private val cancelOrchestratorErrorLog = "Cannot cancel orchestration"
    private val cancelOrchestratorSuccessLog = "cancel orchestration"
    private val resetOrchestratorSessionLog = "Cleared Orchestrator holder session"
    private val startOrchestratorErrorLog = "Cannot start orchestration"
    private val startOrchestratorSuccessLog = "start orchestration"

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
    fun `test start called`() = runTest {
        orchestrator.start(setOf())

        assert(startOrchestratorSuccessLog in logger)
        assert(startOrchestratorErrorLog !in logger)

        assertThat(
            session,
            inPreflight()
        )
    }

    @Test
    fun `Orchestrator cannot be started more than once`() = runTest {
        `test start called`()

        orchestrator.start(setOf())

        assert(startOrchestratorErrorLog in logger)
        assertThat(
            session,
            inPreflight()
        )
    }

    @Test
    fun `Orchestrator cannot cancel invalid state transitions`(
        @TestParameter(valuesProvider = UncancellableHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        initialState = state
        orchestrator.cancel()

        assert(cancelOrchestratorErrorLog in logger)
        assert(cancelOrchestratorSuccessLog !in logger)
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

        assert(cancelOrchestratorSuccessLog in logger)
        assert(cancelOrchestratorErrorLog !in logger)
        assertThat(
            session,
            hasCurrentState(isCancelled())
        )
    }

    @Test
    fun `Resetting the Orchestrator clears the HolderSession`() = runTest {
        `test start called`()

        orchestrator.reset()

        assert(resetOrchestratorSessionLog in logger)
        assertThat(
            session,
            hasCurrentState(HolderSessionState.NotStarted)
        )
    }
}
