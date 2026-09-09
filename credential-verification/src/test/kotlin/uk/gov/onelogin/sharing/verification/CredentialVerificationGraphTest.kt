package uk.gov.onelogin.sharing.verification

import dev.zacsweers.metro.createGraphFactory
import io.mockk.mockk
import java.security.cert.X509Certificate
import org.junit.Assert.assertNotNull
import org.junit.Test

class CredentialVerificationGraphTest {
    private val certificate: X509Certificate = mockk(relaxed = true)

    @Test
    fun `Graph instances are created with an X509 Certificate`() {
        val graph = createGraphFactory<CredentialVerificationGraph.Factory>().create(
            certificate
        )

        assertNotNull(graph.getDocumentVerifier())
        assertNotNull(graph.getTrustVerifier())
    }
}
