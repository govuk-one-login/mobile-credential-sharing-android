package uk.gov.onelogin.orchestration

import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger

class HolderOrchestratorTest {
    private val logger = SystemLogger()

    @Test
    fun `test start called`() {
        val orchestrator = HolderOrchestrator(logger)
        orchestrator.start()

        assert(logger.contains("start orchestration"))
    }

    @Test
    fun `test cancel called`() {
        val orchestrator = HolderOrchestrator(logger)
        orchestrator.cancel()

        assert(logger.contains("cancel orchestration"))
    }
}
