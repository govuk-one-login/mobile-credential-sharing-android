package uk.gov.onelogin.sharing.orchestration.verificationrequest

import io.mockk.mockk
import java.security.cert.X509Certificate
import org.junit.Assert.assertEquals
import org.junit.Test

class VerifierConfigTest {

    @Test
    fun `stores verification request and trusted certificates`() {
        val request = VerificationRequest.raw(
            "org.iso.18013.5.1.mDL",
            mapOf("given_name" to true)
        )
        val config = VerifierConfig(
            verificationRequest = request,
            trustedRootCertificate = mockk()
        )

        assertEquals(request, config.verificationRequest)
    }

    @Test
    fun `data class equality`() {
        val certificate: X509Certificate = mockk()
        val request = VerificationRequest.raw("type", mapOf("a" to true))
        val a = VerifierConfig(request, certificate)
        val b = VerifierConfig(request, certificate)

        assertEquals(a, b)
    }
}
