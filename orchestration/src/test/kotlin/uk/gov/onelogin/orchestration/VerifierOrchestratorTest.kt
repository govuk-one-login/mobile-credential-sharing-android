package uk.gov.onelogin.orchestration

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Before
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS

class VerifierOrchestratorTest {
    private val logger = SystemLogger()
    private val orchestrator by lazy {
        VerifierOrchestrator(logger)
    }

    @Before
    fun setUp() {
        assertEquals(
            0,
            logger.size
        )
    }

    @Test
    fun `logs correctly on start`() {
        orchestrator.start(setOf())
        assert(START_ORCHESTRATION_SUCCESS in logger)
    }

    @Test
    fun `logs correctly on cancel`() {
        orchestrator.cancel()
        assert(CANCEL_ORCHESTRATION_SUCCESS in logger)
    }

    @Test
    fun `logs correctly on reset`() {
        orchestrator.reset()
        assert("Cleared Orchestrator verifier session" in logger)
    }
}
