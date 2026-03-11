package uk.gov.onelogin.sharing.di.internal

import dev.zacsweers.metro.createGraphFactory
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.CredentialVerifierNew
import uk.gov.onelogin.sharing.CredentialVerifierNewImpl
import uk.gov.onelogin.sharing.VerificationRequestNew
import uk.gov.onelogin.sharing.VerifierCredentialSdk
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.VerifierCredentialGraph

class VerifierCredentialSdkImpl(private val appGraph: CredentialSharingAppGraph) :
    VerifierCredentialSdk {

    override fun verifier(): CredentialVerifierNew {
        val orchestrator = createGraphFactory<VerifierCredentialGraph.Factory>()
            .create(appGraph)
            .verifierOrchestrator()

        return CredentialVerifierNewImpl(
            appGraph = appGraph,
            orchestrator = orchestrator,
            verificationRequest = VerificationRequestNew(),
            trustedCertificates = listOf(),
        )
    }
}