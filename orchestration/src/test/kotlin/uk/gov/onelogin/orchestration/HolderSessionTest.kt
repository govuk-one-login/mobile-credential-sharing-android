package uk.gov.onelogin.orchestration

import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger

class HolderSessionTest {
    private val logger = SystemLogger()

    @Test
    fun `transition to state`() {
        val session = HolderSession(logger)
        session.transitionToState("started")

        assert(logger.contains("Transitioning to state: started"))
    }
}
