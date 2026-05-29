package uk.gov.onelogin.sharing.sdk.internal.verifier

import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.verification.CredentialVerificationGraph

class VerifyCredentialSdkImpl(
    private val appGraph: CredentialSharingAppGraph,
    private val verifierGraphFactory: VerifyCredentialGraph.Factory,
    private val credentialVerificationGraphFactory: CredentialVerificationGraph.Factory,
) : VerifyCredentialSdk {

    override fun verifier(verifierConfig: VerifierConfig): CredentialVerifier {
        val credentialVerificationGraph = credentialVerificationGraphFactory
            .create(verifierConfig.trustedRootCertificate)
        val orchestrator = verifierGraphFactory
            .create(
                appGraph = appGraph,
                credentialVerificationGraph = credentialVerificationGraph,
                verifierConfig = verifierConfig
            )
            .verifierOrchestrator()

        return CredentialVerifierImpl(
            appGraph = appGraph,
            orchestrator = orchestrator,
            verificationRequest = verifierConfig.verificationRequest,
        )
    }
}
