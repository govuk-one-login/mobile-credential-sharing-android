package uk.gov.onelogin.sharing.di.internal.verifier

import uk.gov.onelogin.VerifierConfig
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialGraph
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialSdk

class VerifierCredentialSdkImpl(
    private val appGraph: CredentialSharingAppGraph,
    private val verifierGraphFactory: VerifierCredentialGraph.Factory
) : VerifierCredentialSdk {

    override fun verifier(verifierConfig: VerifierConfig): CredentialVerifier {
        val orchestrator = verifierGraphFactory
            .create(
                appGraph = appGraph,
                verifierConfig = verifierConfig
            )
            .verifierOrchestrator()

        return CredentialVerifierImpl(
            appGraph = appGraph,
            orchestrator = orchestrator,
            verificationRequest = verifierConfig.verificationRequest,
            trustedCertificates = verifierConfig.trustedCertificates
        )
    }
}
