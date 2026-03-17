package uk.gov.onelogin.sharing.sdk.internal.verifier

import uk.gov.onelogin.sharing.orchestration.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk

class VerifyCredentialSdkImpl(
    private val appGraph: CredentialSharingAppGraph,
    private val verifierGraphFactory: VerifyCredentialGraph.Factory
) : VerifyCredentialSdk {

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
