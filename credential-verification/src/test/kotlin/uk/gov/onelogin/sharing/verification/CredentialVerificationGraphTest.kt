package uk.gov.onelogin.sharing.verification

import dev.zacsweers.metro.createGraphFactory
import io.mockk.mockk
import java.security.cert.X509Certificate
import org.junit.Assert.assertNotNull
import org.junit.Test
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger

class CredentialVerificationGraphTest {
    private val certificate: X509Certificate = mockk(relaxed = true)
    private val logger: Logger = SystemLogger()

    @Test
    fun `Graph instances are created with an X509 Certificate`() {
        val graph = createGraphFactory<CredentialVerificationGraph.Factory>().create(
            certificate,
            logger
        )

        assertNotNull(graph.getDocumentVerifier())
        assertNotNull(graph.getTrustVerifier())
    }
}
