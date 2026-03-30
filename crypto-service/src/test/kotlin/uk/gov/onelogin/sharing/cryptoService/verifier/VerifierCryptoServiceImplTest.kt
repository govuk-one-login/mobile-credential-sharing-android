package uk.gov.onelogin.sharing.cryptoService.verifier

import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger

class VerifierCryptoServiceImplTest {
    private val logger = SystemLogger()
    private val service = VerifierCryptoServiceImpl(logger)

    @Test
    fun `processEngagement logs debug message`() {
        service.processEngagement("")

        assert("processEngagement called — not yet implemented" in logger)
    }
}
