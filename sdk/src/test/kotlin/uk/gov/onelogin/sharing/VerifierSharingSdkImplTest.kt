package uk.gov.onelogin.sharing

import io.mockk.every
import io.mockk.mockk
import java.security.cert.Certificate
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.VerificationRequest
import uk.gov.onelogin.VerifierConfig
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.di.internal.verifier.CredentialVerifierImpl
import uk.gov.onelogin.sharing.di.internal.verifier.VerifyCredentialSdkImpl

class VerifierSharingSdkImplTest {
    private val appGraph = mockk<CredentialSharingAppGraph>()
    private val verifierGraphFactory = mockk<VerifyCredentialGraph.Factory>()
    private val verifierGraph = mockk<VerifyCredentialGraph>()
    private val orchestrator = mockk<Orchestrator.Verifier>()

    @Test
    fun `verifier returns CredentialVerifier with expected dependencies`() {
        val verificationRequest = VerificationRequest(
            documentType = "org.iso.18013.5.1.mDL",
            requestedElements = listOf("given_name", "family_name")
        )
        val trustedCertificates = emptyList<Certificate>()
        val verifierConfig = VerifierConfig(
            verificationRequest = verificationRequest,
            trustedCertificates = trustedCertificates
        )

        every {
            verifierGraphFactory.create(
                appGraph = appGraph,
                verifierConfig = verifierConfig
            )
        } returns verifierGraph
        every { verifierGraph.verifierOrchestrator() } returns orchestrator

        val sdk = VerifyCredentialSdkImpl(
            appGraph = appGraph,
            verifierGraphFactory = verifierGraphFactory
        )

        val result = sdk.verifier(verifierConfig)

        assertTrue(result is CredentialVerifierImpl)

        result as CredentialVerifierImpl
        assertSame(appGraph, result.appGraph)
        assertSame(orchestrator, result.orchestrator)
    }
}
