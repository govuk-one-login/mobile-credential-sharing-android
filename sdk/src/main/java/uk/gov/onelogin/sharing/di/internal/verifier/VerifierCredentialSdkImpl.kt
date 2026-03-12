package uk.gov.onelogin.sharing.di.internal.verifier

import java.security.cert.Certificate
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.di.api.verifier.VerificationRequest
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialGraph
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialSdk

class VerifierCredentialSdkImpl(
    private val appGraph: CredentialSharingAppGraph,
    private val verifierGraphFactory: VerifierCredentialGraph.Factory
) : VerifierCredentialSdk {

    override fun verifier(
        verificationRequest: VerificationRequest,
        trustedCertificates: List<Certificate>
    ): CredentialVerifier {
        val orchestrator = verifierGraphFactory
            .create(appGraph)
            .verifierOrchestrator()

        return CredentialVerifierImpl(
            appGraph = appGraph,
            orchestrator = orchestrator,
            verificationRequest = verificationRequest,
            trustedCertificates = trustedCertificates
        )
    }
}
