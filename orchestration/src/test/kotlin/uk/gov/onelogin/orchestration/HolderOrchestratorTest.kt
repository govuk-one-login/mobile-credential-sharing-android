package uk.gov.onelogin.orchestration

import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

class HolderOrchestratorTest {

    @Test
    fun `test start called`() {
        val orchestrator = FakeOrchestrator()
        orchestrator.start()

        assertEquals(1, orchestrator.startCount)
    }

    @Test
    fun `test cancel called`() {
        val orchestrator = FakeOrchestrator()
        orchestrator.cancel()

        assertEquals(1, orchestrator.cancelCount)
    }
}
