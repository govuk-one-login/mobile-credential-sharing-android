package uk.gov.onelogin.sharing.orchestration.verificationrequest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifierConfigTest {

    @Test
    fun `stores verification request and trusted certificates`() {
        val request = VerificationRequest.raw("org.iso.18013.5.1.mDL", listOf("given_name"))
        val config = VerifierConfig(
            verificationRequest = request,
            trustedCertificates = emptyList()
        )

        assertEquals(request, config.verificationRequest)
        assertTrue(config.trustedCertificates.isEmpty())
    }

    @Test
    fun `data class equality`() {
        val request = VerificationRequest.raw("type", listOf("a"))
        val a = VerifierConfig(request, emptyList())
        val b = VerifierConfig(request, emptyList())

        assertEquals(a, b)
    }
}
