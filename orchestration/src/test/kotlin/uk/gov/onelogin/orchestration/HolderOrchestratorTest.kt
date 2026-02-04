package uk.gov.onelogin.orchestration

import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

class HolderOrchestratorTest {

    @Test
    fun `start returns true`() {
        val orchestrator = FakeOrchestrator()
        orchestrator.start()

        assertEquals(1, orchestrator.startCount)
    }
}
