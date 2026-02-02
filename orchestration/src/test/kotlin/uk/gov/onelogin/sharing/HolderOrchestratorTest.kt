package uk.gov.onelogin.sharing

import org.junit.Assert.assertTrue
import org.junit.Test

class HolderOrchestratorTest {

    @Test
    fun `start returns true`() {
        val orchestrator = HolderOrchestratorStub.orchestrator
        val result = orchestrator.start()

        assertTrue(result)
    }
}
