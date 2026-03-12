package uk.gov.onelogin.sharing.di.api.verifier

import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

interface CredentialVerifier {
    val appGraph: CredentialSharingAppGraph

    val orchestrator: Orchestrator.Verifier
}
