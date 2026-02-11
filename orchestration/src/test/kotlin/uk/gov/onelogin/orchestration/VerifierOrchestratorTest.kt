package uk.gov.onelogin.orchestration

import kotlin.test.Test
import kotlin.test.assertEquals
import uk.gov.logging.testdouble.SystemLogger

class VerifierOrchestratorTest {
    private val logger = SystemLogger()
    private val orchestrator = VerifierOrchestrator(logger)

    @Test
    fun `logs correctly on start`() {
        assertEquals(0, logger.size)
        orchestrator.start(setOf())
        assert(logger.contains("start orchestration"))
    }

    @Test
    fun `logs correctly on cancel`() {
        assertEquals(0, logger.size)
        orchestrator.cancel()
        assert(logger.contains("cancel orchestration"))
    }

    @Test
    fun `logs correctly on reset`() {
        assertEquals(0, logger.size)
        orchestrator.reset()
        assert(logger.contains("Cleared Orchestrator verifier session"))
    }
}
