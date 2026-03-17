package uk.gov.onelogin.sharing.sdk

import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier

class FakeCredentialVerifier(
    override val appGraph: CredentialSharingAppGraph,
    override val orchestrator: Orchestrator.Verifier
) : CredentialVerifier
