package uk.gov.onelogin.orchestration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers.hasCurrentState
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionImpl
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.session.verifier.matchers.VerifierSessionStateMatchers.inPreflight

class VerifierOrchestratorTest {
    private var initialState: VerifierSessionState = VerifierSessionState.NotStarted
    private val logger = SystemLogger()
    private val session by lazy {
        VerifierSessionImpl(
            logger = logger,
            internalState = MutableStateFlow(initialState)
        )
    }
    private val orchestrator by lazy {
        VerifierOrchestrator(
            logger = logger,
            session = session,
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
    fun `logs correctly on cancel`() = runTest {
        orchestrator.cancel()
        assert(CANCEL_ORCHESTRATION_SUCCESS in logger)
    }

    @Test
    fun `logs correctly on reset`() = runTest {
        orchestrator.reset()
        assert("Cleared Orchestrator verifier session" in logger)
    }
}
